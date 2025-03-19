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
public class CSharpDefaultAdditionalTextAttributesProvider implements EditorColorSchemeExtender {
    @Override
    public void extend(Builder builder) {
        builder.add(CSharpHighlightKey.DISABLED_BLOCK, AttributesFlyweightBuilder.create()
            .withBackground(new RGBColor(0xF0, 0xF0, 0xF0))
            .build());

        builder.add(CSharpHighlightKey.DELEGATE_METHOD_NAME, AttributesFlyweightBuilder.create()
            .withForeground(new RGBColor(0x72, 0x9F, 0x4C))
            .build());

        builder.add(CSharpHighlightKey.METHOD_REF, AttributesFlyweightBuilder.create()
            .withBackground(new RGBColor(0xFF, 0xE4, 0xA5))
            .build());

        builder.add(CSharpHighlightKey.IMPLICIT_OR_EXPLICIT_CAST, AttributesFlyweightBuilder.create()
            .withBackground(new RGBColor(0xDF, 0xFF, 0xFD))
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
        return EditorColorsScheme.DEFAULT_SCHEME_NAME;
    }

}
