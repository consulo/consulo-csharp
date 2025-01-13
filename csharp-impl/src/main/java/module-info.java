/**
 * @author VISTALL
 * @since 10-Sep-22
 */
open module consulo.csharp
{
	// TODO remove this dependency in future
	requires java.desktop;
	// TODO remove this dependency in future
	requires forms.rt;
	// TODO remove this dependency in future
	requires consulo.ide.impl;

	requires transitive consulo.csharp.base.impl;
	requires transitive consulo.csharp.psi.impl;
	requires transitive consulo.dotnet.impl;
	requires transitive consulo.dotnet.debugger.api;
	requires transitive consulo.dotnet.debugger.impl;
	requires transitive consulo.csharp.composite.formatting.string.impl;
	requires transitive consulo.csharp.doc.psi.impl;
	requires transitive consulo.internal.dotnet.asm;
	requires transitive consulo.internal.dotnet.msil.decompiler;

	exports consulo.csharp.impl.compiler;
	exports consulo.csharp.impl.ide;
	exports consulo.csharp.impl.ide.actions;
	exports consulo.csharp.impl.ide.actions.generate;
	exports consulo.csharp.impl.ide.actions.generate.memberChoose;
	exports consulo.csharp.impl.ide.actions.navigate;
	exports consulo.csharp.impl.ide.assemblyInfo;
	exports consulo.csharp.impl.ide.assemblyInfo.blocks;
	exports consulo.csharp.impl.ide.codeInsight;
	exports consulo.csharp.impl.ide.codeInsight.actions;
	exports consulo.csharp.impl.ide.codeInsight.actions.expressionActions.lambda;
	exports consulo.csharp.impl.ide.codeInsight.editorActions;
	exports consulo.csharp.impl.ide.codeInsight.highlighting;
	exports consulo.csharp.impl.ide.codeInsight.hits;
	exports consulo.csharp.impl.ide.codeInsight.moveUpDown;
	exports consulo.csharp.impl.ide.codeInsight.problems;
	exports consulo.csharp.impl.ide.codeInsight.template.postfix;
	exports consulo.csharp.impl.ide.codeInspection;
	exports consulo.csharp.impl.ide.codeInspection.languageKeywordUsage;
	exports consulo.csharp.impl.ide.codeInspection.matchNamespace;
	exports consulo.csharp.impl.ide.codeInspection.obsolete;
	exports consulo.csharp.impl.ide.codeInspection.unnecessaryCast;
	exports consulo.csharp.impl.ide.codeInspection.unnecessaryEnumUnderlyingType;
	exports consulo.csharp.impl.ide.codeInspection.unnecessaryModifier;
	exports consulo.csharp.impl.ide.codeInspection.unnecessaryNamedArgument;
	exports consulo.csharp.impl.ide.codeInspection.unnecessarySemicolon;
	exports consulo.csharp.impl.ide.codeInspection.unnecessaryType;
	exports consulo.csharp.impl.ide.codeInspection.unusedUsing;
	exports consulo.csharp.impl.ide.codeStyle;
	exports consulo.csharp.impl.ide.completion;
	exports consulo.csharp.impl.ide.completion.expected;
	exports consulo.csharp.impl.ide.completion.filter;
	exports consulo.csharp.impl.ide.completion.insertHandler;
	exports consulo.csharp.impl.ide.completion.item;
	exports consulo.csharp.impl.ide.completion.patterns;
	exports consulo.csharp.impl.ide.completion.smartEnter;
	exports consulo.csharp.impl.ide.completion.util;
	exports consulo.csharp.impl.ide.completion.weigher;
	exports consulo.csharp.impl.ide.copyright;
	exports consulo.csharp.impl.ide.debugger;
	exports consulo.csharp.impl.ide.debugger.expressionEvaluator;
	exports consulo.csharp.impl.ide.documentation;
	exports consulo.csharp.impl.ide.editor;
	exports consulo.csharp.impl.ide.findUsage;
	exports consulo.csharp.impl.ide.findUsage.groupingRule;
	exports consulo.csharp.impl.ide.findUsage.referenceSearch;
	exports consulo.csharp.impl.ide.findUsage.usageType;
	exports consulo.csharp.impl.ide.highlight;
	exports consulo.csharp.impl.ide.highlight.check;
	exports consulo.csharp.impl.ide.highlight.check.impl;
	exports consulo.csharp.impl.ide.highlight.quickFix;
	exports consulo.csharp.impl.ide.highlight.util;
	exports consulo.csharp.impl.ide.idCache;
	exports consulo.csharp.impl.ide.lineMarkerProvider;
	exports consulo.csharp.impl.ide.liveTemplates.context;
	exports consulo.csharp.impl.ide.liveTemplates.expression;
	exports consulo.csharp.impl.ide.liveTemplates.macro;
	exports consulo.csharp.impl.ide.msil.representation;
	exports consulo.csharp.impl.ide.msil.representation.builder;
	exports consulo.csharp.impl.ide.navbar;
	exports consulo.csharp.impl.ide.navigation;
	exports consulo.csharp.impl.ide.newProjectOrModule;
	exports consulo.csharp.impl.ide.parameterInfo;
	exports consulo.csharp.impl.ide.presentation;
	exports consulo.csharp.impl.ide.projectView;
	exports consulo.csharp.impl.ide.projectView.impl;
	exports consulo.csharp.impl.ide.refactoring;
	exports consulo.csharp.impl.ide.refactoring.changeSignature;
	exports consulo.csharp.impl.ide.refactoring.copy;
	exports consulo.csharp.impl.ide.refactoring.extractMethod;
	exports consulo.csharp.impl.ide.refactoring.inlineAction;
	exports consulo.csharp.impl.ide.refactoring.introduceVariable;
	exports consulo.csharp.impl.ide.refactoring.move;
	exports consulo.csharp.impl.ide.refactoring.rename;
	exports consulo.csharp.impl.ide.refactoring.rename.inplace;
	exports consulo.csharp.impl.ide.refactoring.util;
	exports consulo.csharp.impl.ide.resolve;
	exports consulo.csharp.impl.ide.structureView;
	exports consulo.csharp.impl.ide.structureView.sorters;
	exports consulo.csharp.impl.ide.surroundWith;
	exports consulo.csharp.impl.lang;
	exports consulo.csharp.impl.lang.doc.inspection;
	exports consulo.csharp.impl.lang.formatter;
	exports consulo.csharp.impl.lang.formatter.processors;
	exports consulo.csharp.impl.lang.psi;
	exports consulo.csharp.impl.libraryAnalyzer;
	exports consulo.csharp.impl.localize;
}