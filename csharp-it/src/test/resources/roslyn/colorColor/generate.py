# Generates the RoslynColorColorTest sources from Roslyn's ColorColorTests.cs (the "Color Color" rule, C# spec 12.8.7.2):
# the source of each test, with a marker comment at every reference the test asserts on.
#
#   /*@symbol:S*/ref        - the reference right after the marker resolves to S
#   ref/*@parent:S*/.Member - the member access whose qualifier is the reference right before the marker resolves to S
#
# Roslyn's /*<bind>*/X/*</bind>*/ becomes /*@symbol:S*/X/*@parent:S*/. S is "<Kind> <signature>":
#
#   Type <qualified name>                     NamedType (an alias stands for the type it names, as GetSymbolInfo does)
#   Field|Property|Event <type> <Owner>.<Name>
#   Parameter|Local <type> <name>
#   Method <return type> <Owner>.<Name>[<T, ...>](<modifiers> <type> <name>, ...)   (params/ref/out/in/this)
#
# with C# keywords for the predefined types (int, not System.Int32). The display strings of Roslyn are normalized to
# that form; where Roslyn shows something a declaration can not (a constructed generic method, a reduced extension
# method) the expected symbol is the declaration - see OVERRIDES.
#
# Usage: python3 generate.py [<roslyn checkout> [<output directory>]]
import os, re, sys
sys.dont_write_bytecode = True
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import roslyn_src

TEST_FILE = 'ColorColorTests.cs'

KINDS = {'NamedType': 'Type', 'Field': 'Field', 'Property': 'Property', 'Event': 'Event', 'Parameter': 'Parameter',
         'Local': 'Local', 'Method': 'Method'}

KEYWORDS = {'Boolean': 'bool', 'Byte': 'byte', 'SByte': 'sbyte', 'Char': 'char', 'Decimal': 'decimal',
            'Double': 'double', 'Single': 'float', 'Int16': 'short', 'UInt16': 'ushort', 'Int32': 'int',
            'UInt32': 'uint', 'Int64': 'long', 'UInt64': 'ulong', 'Object': 'object', 'String': 'string',
            'Void': 'void'}

# test -> {'symbol'|'parent': expected} replacing what CheckExpressionAndParent says
OVERRIDES = {
    # Roslyn shows the constructed method M<int>; a reference resolves to the declaration
    'TestGenericMethodOnRight': {'parent': 'Method void E.M<T>(T x)'},
    'TestGenericMethodGroupOnRight': {'parent': 'Method void E.M<T>(T x)'},
    # Roslyn shows the extension method reduced to the receiver type E; a reference resolves to the declaration
    'TestExtensionMethodOnRight': {'parent': 'Method void Extensions.M(this E e, int x)'},
    'TestExtensionMethodGroupOnRight': {'parent': 'Method void Extensions.M(this E e, int x)'},
}

# tests which do not use CheckExpressionAndParent: the symbols they assert on /*<bind>*/ (None - not asserted,
# or no symbol: a candidate, an overload resolution failure)
BIND = {
    # asserts the alias Q=E with GetAliasInfo; GetSymbolInfo of the alias is the type E
    'TestAliasMemberNameMatchesAlias2': ('Type E', 'Method void E.M(params int[] a)'),
    'TestTypeOrValueInMethodGroupIsExpression': ('Local Color Color', None),
    'TestErrorLookup': ('Property E F.E', None),
    'TestErrorLookupMethodGroup': ('Property E F.E', None),
    'TestErrorOverloadResolution': ('Property E F.E', None),
    'TestErrorOverloadResolutionMethodGroup': ('Property E F.E', None),
    # E is not a value (a candidate only); the parent is still delegate Invoke - Roslyn checks the name only
    'TestErrorNonFieldLikeEvent': (None, 'Method void E.Invoke()'),
    'TestInEnumDecl': ('Field Color.Color', None),
}

# tests without /*<bind>*/: (needle with | at the reference, expected symbol)
NEEDLES = {
    'Bug969006_1': [('= |E.A', 'Type E'), ('var z = |E;', 'Local E E')],
    'Bug969006_3': [('= |E.A', 'Type E'), ('var z = |E;', 'Local E E')],
    'TestColorColorSymbolInfoInArrowExpressionClauseSyntax': [('=> |Lifetime.Persistent', 'Type Lifetime')],
    'TestColorColorSymbolInfoInArrowExpressionClauseSyntax_2': [('=> |Lifetime.Persistent', 'Type Lifetime')],
}

# every other test of the file, with the reason it is not ported (README.txt lists them too)
SKIPPED = {
    'TestNonInitializedLocalOnLeft': 'diagnostics only',
    'StackOverflow01': 'checks only that binding an incomplete lambda does not crash, no symbol',
    'GenericTypeOk': 'diagnostics only',
    'FieldOfEnumType': 'diagnostics only',
    'AlternateTypeAndVariable': 'diagnostics only',
    'FieldAndMethodSameName': 'diagnostics only',
    'TypeAndMethodSameName': 'diagnostics only',
    'ExtensionMethodWithColorColorReceiver': 'checks the emitted IL (boxing)',
    'ShadowedTypeReceiver_1': 'needs a second compiled assembly declaring the same type; checks runtime output',
    'ShadowedTypeReceiver_2': 'needs a second compiled assembly declaring the same type; checks runtime output',
    'ShadowedTypeReceiver_3': 'needs a second compiled assembly declaring the same type; checks runtime output',
    'ShadowedTypeReceiver_4': 'needs a second compiled assembly declaring the same type; checks runtime output',
    'RangeVariableColorColor': 'diagnostics and runtime output only',
    'Bug969006_2': 'the expected symbol is a local of an error type (? E)',
    'Bug969006_4': 'the expected symbol is a local of an error type (? E)',
    'VersionUnificationConstColorColorEnum': 'needs a mscorlib 2.0 reference assembly; diagnostics only',
    'PatternMatchToBaseTypeWithUseSiteWarningOnBaseType': 'needs a mscorlib 2.0 reference assembly; diagnostics only',
    'ObsoleteConstColorColorEnum': 'diagnostics only',
    'WorkItem718761': 'diagnostics only',
    'WorkItem61171_Constraints': 'diagnostics only (theory)',
    'WorkItem61171_DynamicObjectTuple': 'diagnostics only (theory)',
    'WorkItem61171_ModoptObject': 'needs an IL assembly with modopt; diagnostics only',
    'ConstFieldEnum_01': 'runtime output and diagnostics only',
    'ConstFieldEnum_02': 'diagnostics only',
    'ConstFieldPrimitives': 'runtime output and diagnostics only',
    'ConstFieldPrimitivesActualCircular': 'diagnostics only',
    'ConstInAttributes_NoCycle_01': 'diagnostics only',
    'ConstInAttributes_NoCycle_02': 'runtime output and diagnostics only',
}

CHECK = re.compile(r'CheckExpressionAndParent\(text,\s*SymbolKind\.(\w+),\s*"([^"]*)",\s*SymbolKind\.(\w+),\s*"([^"]*)"')
BIND_MARK = re.compile(r'/\*<bind>\*/(\w+)/\*</bind>\*/')


def test_body(text, test):
    # up to the next member of the test class: a source may close a block at the indentation of the test method
    start = re.search(r'\n        public void ' + test + r'\(', text).end()
    end = re.compile(r'\n        (\[|#|private void |class )').search(text, start)
    return text[start:end.start() if end else len(text)]


def keywords(display):
    return re.sub(r'\bSystem\.(' + '|'.join(KEYWORDS) + r')\b(?!\.)', lambda m: KEYWORDS[m.group(1)], display)


def normalize(kind, display):
    """Roslyn's SymbolKind and ToTestDisplayString() as '<Kind> <signature>'."""
    display = keywords(display)
    if kind == 'Property':
        display = re.sub(r'\s*\{[^}]*\}$', '', display)
    elif kind == 'Event':
        display = re.sub(r'^event ', '', display)
    elif kind == 'Field' and ' ' not in display:
        # an enum member is shown without a type: its type is the enum
        display = display.rsplit('.', 1)[0] + ' ' + display
    return KINDS[kind] + ' ' + display


def with_field_type(expected):
    kind, display = expected.split(' ', 1)
    return normalize(kind, display) if kind == 'Field' else expected


def bind(source, symbol, parent):
    def replace(m):
        return ('' if symbol is None else '/*@symbol:' + symbol + '*/') + m.group(1) + ('' if parent is None else '/*@parent:' + parent + '*/')
    result, count = BIND_MARK.subn(replace, source)
    if count != 1:
        raise ValueError('expected one /*<bind>*/, found %d' % count)
    return result


def mark(source, needle, expected):
    before, after = needle.split('|')
    index = source.find(before + after)
    if index < 0:
        raise ValueError('no match of ' + needle)
    insert = index + len(before)
    return source[:insert] + '/*@symbol:' + expected + '*/' + source[insert:]


def main(roslyn, output):
    roslyn_src.R = os.path.join(roslyn, 'src/Compilers/CSharp/Test/Semantic/Semantics') + '/'
    with open(roslyn_src.R + TEST_FILE, encoding='utf-8-sig') as f:
        text = f.read()
    tests = re.findall(r'\n        public void (\w+)\(', text)

    ported = 0
    for test in tests:
        if test in SKIPPED:
            continue
        body = test_body(text, test)
        source = roslyn_src.sources(body)[0]
        check = CHECK.search(body)
        if test in NEEDLES:
            for needle, expected in NEEDLES[test]:
                source = mark(source, needle, expected)
        elif test in BIND:
            symbol, parent = BIND[test]
            source = bind(source, symbol and with_field_type(symbol), parent)
        elif check:
            override = OVERRIDES.get(test, {})
            symbol = override.get('symbol', normalize(check.group(1), check.group(2)))
            parent = override.get('parent', normalize(check.group(3), check.group(4)))
            source = bind(source, symbol, parent)
        else:
            raise ValueError('test %s is neither ported nor skipped' % test)

        with open(os.path.join(output, test + '.cs'), 'w', encoding='utf-8') as out:
            out.write(source.lstrip('\n').replace('\r\n', '\n'))
        ported += 1

    print('ported %d, skipped %d' % (ported, len(SKIPPED)))


if __name__ == '__main__':
    here = os.path.dirname(os.path.abspath(__file__))
    main(sys.argv[1] if len(sys.argv) > 1 else '/run/media/vistall/repositories/roslyn',
         sys.argv[2] if len(sys.argv) > 2 else here)
