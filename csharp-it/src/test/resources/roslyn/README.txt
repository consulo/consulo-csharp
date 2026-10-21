BinaryOperatorEasyOut.cs, UnaryOperatorEasyOut.cs and ConversionEasyOut.cs are copied unchanged from https://github.com/dotnet/roslyn
(src/Compilers/CSharp/Portable/Binder/Semantics/Operators and .../Conversions, commit d29ff04c2688c4c44a4540ee66cb11e46f0825d5), MIT license, see the file headers.

They are the tables of the operators C# picks and the conversions it classifies for the predefined types -
RoslynOperatorMatrixTest and RoslynConversionMatrixTest check Consulo against them.

colorColor/ - the "Color Color" tests of Roslyn (src/Compilers/CSharp/Test/Semantic/Semantics/ColorColorTests.cs, commit
d29ff04c2688c4c44a4540ee66cb11e46f0825d5, MIT license), one <TestName>.cs per test with marker comments for what the
references resolve to, written by colorColor/generate.py - RoslynColorColorTest checks Consulo against them.
Not ported (no symbol to check, or a setup the test module can not express):
  TestNonInitializedLocalOnLeft, GenericTypeOk, FieldOfEnumType, AlternateTypeAndVariable, FieldAndMethodSameName,
  TypeAndMethodSameName, ObsoleteConstColorColorEnum, WorkItem718761, WorkItem61171_Constraints,
  WorkItem61171_DynamicObjectTuple, ConstFieldEnum_02, ConstFieldPrimitivesActualCircular,
  ConstInAttributes_NoCycle_01 - diagnostics only
  RangeVariableColorColor, ConstFieldEnum_01, ConstFieldPrimitives, ConstInAttributes_NoCycle_02 - diagnostics and
  runtime output only
  StackOverflow01 - checks only that binding an incomplete lambda does not crash
  ExtensionMethodWithColorColorReceiver - checks the emitted IL
  ShadowedTypeReceiver_1..4 - need a second compiled assembly declaring the same type
  VersionUnificationConstColorColorEnum, PatternMatchToBaseTypeWithUseSiteWarningOnBaseType - need a mscorlib 2.0
  reference assembly; diagnostics only
  WorkItem61171_ModoptObject - needs an IL assembly with modopt; diagnostics only
  Bug969006_2, Bug969006_4 - the expected symbol is a local of an error type
Ported partially (only the symbols Roslyn asserts; a null symbol or a set of candidates is not checked):
  TestTypeOrValueInMethodGroupIsExpression, TestErrorLookup, TestErrorLookupMethodGroup, TestErrorOverloadResolution,
  TestErrorOverloadResolutionMethodGroup, TestInEnumDecl - the qualifier only
  TestErrorNonFieldLikeEvent - the member access only
