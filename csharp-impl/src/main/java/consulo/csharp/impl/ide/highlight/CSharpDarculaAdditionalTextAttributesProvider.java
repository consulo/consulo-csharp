/*
 * Copyright 2013-2022 consulo.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package consulo.csharp.impl.ide.highlight;

import consulo.annotation.component.ExtensionImpl;
import consulo.colorScheme.AttributesFlyweightBuilder;
import consulo.colorScheme.EditorColorSchemeExtender;
import consulo.colorScheme.EditorColorsScheme;
import consulo.colorScheme.EffectType;
import consulo.ui.color.RGBColor;
import jakarta.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 11-Sep-22
 */
@ExtensionImpl
public class CSharpDarculaAdditionalTextAttributesProvider implements EditorColorSchemeExtender {
    @Override
    public void extend(Builder builder) {
        builder.add(CSharpHighlightKey.DISABLED_BLOCK, AttributesFlyweightBuilder.create()
            .withBackground(new RGBColor(0x4C, 0x49, 0x3A))
            .build());

        builder.add(CSharpHighlightKey.DELEGATE_METHOD_NAME, AttributesFlyweightBuilder.create()
            .withForeground(new RGBColor(84, 116, 58))
            .build());

        builder.add(CSharpHighlightKey.METHOD_REF, AttributesFlyweightBuilder.create()
            .withBackground(new RGBColor(0x63, 0x51, 0x38))
            .build());

        builder.add(CSharpHighlightKey.IMPLICIT_OR_EXPLICIT_CAST, AttributesFlyweightBuilder.create()
            .withBackground(new RGBColor(60, 79, 77))
            .build());

        builder.add(CSharpHighlightKey.INSTANCE_EVENT, AttributesFlyweightBuilder.create()
            .withForeground(new RGBColor(0xB0, 0x43, 0x93))
            .withEffect(EffectType.LINE_UNDERSCORE, null)
            .build());

        builder.add(CSharpHighlightKey.STATIC_EVENT, AttributesFlyweightBuilder.create()
            .withForeground(new RGBColor(0xB0, 0x43, 0x93))
            .withItalicFont()
            .withEffect(EffectType.LINE_UNDERSCORE, null)
            .build());
    }

    @Nonnull
    @Override
    public String getColorSchemeId() {
        return EditorColorsScheme.DARCULA_SCHEME_NAME;
    }
}
