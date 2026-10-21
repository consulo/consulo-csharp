# Generates the RoslynLambdaTest sources from the Roslyn semantic model tests of lambdas (LambdaTests.cs,
# LambdaDiscardParametersTests.cs, SimpleLambdaParametersWithModifiersTests.cs): the source of each test, with a marker
# comment right before every node the test asserts on.
#
#   /*@type:T*/expr       - the type of the expression is T
#   /*@symbol:S*/ref      - the reference resolves to S: "Type name" for a variable, "Owner.Name(Types)" for a method
#   /*@lambda:(T, ...)*/  - the parameter types of the lambda
#
# Usage: python3 generate.py <roslyn checkout> <output directory>
import os, sys
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import roslyn_src

# test file, test name, source index, [(needle with | for the marker position, kind, expected, all matches?)]
SPEC = [
    ('LambdaTests.cs', 'ParenthesizedLambdaInCastExpression', 0, [('(byte) (|x + x)', 'type', 'System.Int32', False)]),
    ('LambdaTests.cs', 'CompletionInLambdaInIncompleteInvocation', 0, [('o => |o/* */.', 'type', 'SomeType', False)]),
    ('LambdaTests.cs', 'Bug960755_03', 0, [('(a, b) => |c.Add', 'symbol', 'System.Collections.Generic.ICollection.Add(C)', False)]),
    ('LambdaTests.cs', 'ParameterReference_01', 0, [('a => |a.', 'symbol', 'Program a', False)]),
    ('LambdaTests.cs', 'ParameterReference_02', 0, [('a => |a.', 'symbol', 'Program a', False)]),
    ('LambdaTests.cs', 'ParameterReference_03', 0, [('a => |a.', 'symbol', 'Program a', False)]),
    ('LambdaTests.cs', 'ParameterReference_04', 0, [('a => |a.', 'symbol', 'Program a', False)]),
    ('LambdaTests.cs', 'TestLambdaWithError06', 0, [('{ |e. }', 'type', 'MyArgumentType', False)]),
    ('LambdaTests.cs', 'TestLambdaWithError07', 0, [('x => |x.', 'type', 'System.String', False)]),
    ('LambdaTests.cs', 'TestLambdaWithError08', 0, [('x => |x.', 'type', 'System.String', False)]),
    ('LambdaTests.cs', 'TestLambdaWithError09', 0, [('x => |x.', 'type', 'System.String', False)]),
    ('LambdaTests.cs', 'TestLambdaWithError10', 0, [('x => |x.', 'type', 'System.String', False)]),
    ('LambdaTests.cs', 'TestLambdaWithError11', 0, [('c => |c.', 'type', 'System.Char', False)]),
    ('LambdaTests.cs', 'TestLambdaWithError12', 0, [('a => |a.', 'type', 'System.String', False)]),
    ('LambdaTests.cs', 'TestLambdaWithError13', 0, [('x => |x', 'type', 'System.String', True)]),
    ('LambdaTests.cs', 'TestLambdaWithError15', 0, [('x => |x', 'type', 'System.String', True)]),
    ('LambdaTests.cs', 'TestLambdaWithError16', 0, [('x => |x', 'type', 'System.String', True)]),
    ('LambdaTests.cs', 'TestLambdaWithError17', 0, [('=> |x.ToString', 'type', 'System.String', True)]),
    ('LambdaTests.cs', 'TestLambdaWithError18', 0, [('=> |x.ToString', 'type', 'System.String', True)]),
    ('LambdaTests.cs', 'TestLambdaWithError19', 0, [('=> |x.ToString', 'type', 'System.String', True)]),
    ('LambdaTests.cs', 'InvalidCast', 0, [('? |(Action)', 'type', 'System.Action', False)]),
    ('LambdaTests.cs', 'DelegateConversions_ImplicitlyTypedParameter_RefParameter', 0, [('r2 => |r2', 'symbol', 'R r2', False)]),
    ('LambdaTests.cs', 'ImplicitlyTypedLambdaWithModifier_CSharp13', 0, [('D d = |(ref a)', 'lambda', '(ref System.Int32)', False)]),
    ('SimpleLambdaParametersWithModifiersTests.cs', 'TestOneParameterWithRef', 0, [('D d = |(ref x)', 'lambda', '(ref System.Int32)', False)]),
    ('LambdaDiscardParametersTests.cs', 'DiscardParameters_NotInScope_BindToOutsideLocal', 0, [
        ('(_, _) => ++|_', 'symbol', 'System.Int32 _', False),
        ('(_, a) => ++|_', 'symbol', 'System.Int64 _', False)]),
    ('LambdaDiscardParametersTests.cs', 'DiscardParameters_NotInScope_BindToOutsideLocal_Nested', 0, [('f2 = (_, _) => ++|_', 'symbol', 'System.Int32 _', False)]),
    ('LambdaDiscardParametersTests.cs', 'DiscardParameters_NotInScope_DeclareLocalNamedUnderscoreInside', 0, [('return |_++', 'symbol', 'System.Int64 _', True)]),
]


def mark(source, needle, kind, expected, all_matches):
    before, after = needle.split('|')
    text = before + after
    marker = '/*@' + kind + ':' + expected + '*/'
    result, start, count = source, 0, 0
    while True:
        index = result.find(text, start)
        if index < 0:
            break
        insert = index + len(before)
        result = result[:insert] + marker + result[insert:]
        count += 1
        start = insert + len(marker) + len(after)
        if not all_matches:
            break
    if count == 0:
        raise ValueError('no match of ' + needle)
    return result


def main(roslyn, output):
    roslyn_src.R = os.path.join(roslyn, 'src/Compilers/CSharp/Test/Semantic/Semantics') + '/'
    for file, test, index, markers in SPEC:
        source = roslyn_src.sources(roslyn_src.test_body(file, test))[index]
        for needle, kind, expected, all_matches in markers:
            source = mark(source, needle, kind, expected, all_matches)
        with open(os.path.join(output, file.replace('Tests.cs', '') + '_' + test + '.cs'), 'w', encoding='utf-8') as out:
            out.write(source.lstrip('\n'))


if __name__ == '__main__':
    main(sys.argv[1], sys.argv[2])
