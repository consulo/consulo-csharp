# Generates the RoslynOverloadOutputTest sources from Roslyn tests which run a program and check its output
# (CompileAndVerify(..., expectedOutput: ...)) where the output tells which overload a call bound to: every overload
# prints a literal of its own. The expected output goes to the first line of the source:
#
#   // expectedOutput: 2
#
# A test is taken only when its output can be told without running it: every Console.Write/WriteLine prints a literal,
# and Main runs straight through (no if, loops, try, switch).
#
# Usage: python3 generate.py <roslyn checkout> <output directory>
import os, re, sys
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import roslyn_src

FILES = [
    'src/Compilers/CSharp/Test/Semantic/Semantics/BetterCandidates.cs',
    'src/Compilers/CSharp/Test/Semantic/Semantics/OverloadResolutionTests.cs',
    'src/Compilers/CSharp/Test/Semantic/Semantics/NamedAndOptionalTests.cs',
]

LITERAL = r'(?:-?\d+(?:\.\d+)?[a-zA-Z]?|"(?:[^"\\]|\\.)*"|\'(?:[^\'\\]|\\.)\'|true|false)'
WRITE = re.compile(r'Console\s*\.\s*Write(?:Line)?\s*\(')
LITERAL_WRITE = re.compile(r'Console\s*\.\s*Write(?:Line)?\s*\(\s*(?:' + LITERAL + r')?\s*\)')
CONTROL_FLOW = re.compile(r'\b(?:if|for|foreach|while|do|try|switch|goto|return)\b|\?')


def expected_output(body):
    m = re.search(r'expectedOutput:\s*(@"(?:[^"]|"")*"|"(?:[^"\\]|\\.)*")', body)
    if not m:
        return None
    literal = m.group(1)
    if literal.startswith('@'):
        return literal[2:-1].replace('""', '"')
    return bytes(literal[1:-1], 'utf-8').decode('unicode_escape')


def main_body(source):
    m = re.search(r'static\s+(?:async\s+)?(?:void|int|System\.Threading\.Tasks\.Task|Task)\s+Main\s*\([^)]*\)\s*\{', source)
    if not m:
        return None
    depth, i = 1, m.end()
    while i < len(source) and depth:
        depth += {'{': 1, '}': -1}.get(source[i], 0)
        i += 1
    return source[m.end():i - 1]


def portable(source, output):
    if output is None or not output.strip():
        return False
    writes = WRITE.findall(source)
    if not writes or len(writes) != len(LITERAL_WRITE.findall(source)):
        return False
    body = main_body(source)
    return body is not None and not CONTROL_FLOW.search(body)


def main(roslyn, output_directory):
    taken = 0
    for path in FILES:
        roslyn_src.R = os.path.join(roslyn, os.path.dirname(path)) + '/'
        file = os.path.basename(path)
        text = open(roslyn_src.R + file, encoding='utf-8-sig').read()
        for name in re.findall(r'\n        public (?:async Task|void) (\w+)\(\)', text):
            try:
                body = roslyn_src.test_body(file, name)
                sources = roslyn_src.sources(body)
            except Exception:
                continue
            output = expected_output(body)
            if not sources or not portable(sources[0], output):
                continue
            out = os.path.join(output_directory, file.replace('Tests.cs', '').replace('.cs', '') + '_' + name + '.cs')
            with open(out, 'w', encoding='utf-8') as f:
                f.write('// expectedOutput: ' + output.replace('\r', '').replace('\n', '\\n') + '\n')
                f.write(sources[0].lstrip('\n'))
            taken += 1
    print('taken', taken)


if __name__ == '__main__':
    main(sys.argv[1], sys.argv[2])
