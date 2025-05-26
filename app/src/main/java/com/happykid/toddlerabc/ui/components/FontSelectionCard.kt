package com.happykid.toddlerabc.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.happykid.toddlerabc.model.ToddlerFontFamily
import com.happykid.toddlerabc.model.FontSizeScale

/**
 * Font Selection Card Component
 * Phase 5: Custom fonts integration UI component
 * 
 * Provides font preview and selection functionality for parents
 * to choose appropriate fonts for their toddlers.
 */
@Composable
fun FontSelectionCard(
    selectedFont: ToddlerFontFamily,
    selectedFontSize: FontSizeScale,
    onFontSelected: (ToddlerFontFamily) -> Unit,
    onFontSizeSelected: (FontSizeScale) -> Unit,
    getFontFamily: (String) -> FontFamily,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Font Selection",
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            // Font Family Selection
            Text(
                text = "Choose Font Style:",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ToddlerFontFamily.getAllFonts().forEach { font ->
                    FontFamilyOption(
                        font = font,
                        isSelected = font == selectedFont,
                        onSelected = { onFontSelected(font) },
                        getFontFamily = getFontFamily,
                        fontScale = selectedFontSize.scale
                    )
                }
            }

            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

            // Font Size Selection
            Text(
                text = "Choose Font Size:",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FontSizeScale.values().forEach { size ->
                    FontSizeOption(
                        fontSize = size,
                        isSelected = size == selectedFontSize,
                        onSelected = { onFontSizeSelected(size) },
                        fontFamily = getFontFamily(selectedFont.fontResourceName)
                    )
                }
            }

            // Font Preview
            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            
            Text(
                text = "Preview:",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )

            FontPreview(
                fontFamily = getFontFamily(selectedFont.fontResourceName),
                fontScale = selectedFontSize.scale
            )
        }
    }
}

/**
 * Individual font family option
 */
@Composable
private fun FontFamilyOption(
    font: ToddlerFontFamily,
    isSelected: Boolean,
    onSelected: () -> Unit,
    getFontFamily: (String) -> FontFamily,
    fontScale: Float,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .selectable(
                selected = isSelected,
                onClick = onSelected
            ),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = if (isSelected) {
            CardDefaults.outlinedCardBorder()
        } else null
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = font.displayName,
                        fontSize = (14 * fontScale).sp,
                        fontFamily = getFontFamily(font.fontResourceName),
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                    Text(
                        text = font.description,
                        fontSize = (12 * fontScale).sp,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        }
                    )
                }
                
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Mini preview
            Text(
                text = "ABC abc 123",
                fontSize = (16 * fontScale).sp,
                fontFamily = getFontFamily(font.fontResourceName),
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Individual font size option
 */
@Composable
private fun FontSizeOption(
    fontSize: FontSizeScale,
    isSelected: Boolean,
    onSelected: () -> Unit,
    fontFamily: FontFamily,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .selectable(
                selected = isSelected,
                onClick = onSelected
            ),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = if (isSelected) {
            CardDefaults.outlinedCardBorder()
        } else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = fontSize.displayName,
                    fontSize = (14 * fontSize.scale).sp,
                    fontFamily = fontFamily,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                Text(
                    text = fontSize.description,
                    fontSize = (12 * fontSize.scale).sp,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    }
                )
            }
            
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * Font preview component
 */
@Composable
private fun FontPreview(
    fontFamily: FontFamily,
    fontScale: Float,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "ABC abc 123",
                fontSize = (24 * fontScale).sp,
                fontFamily = fontFamily,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Hello Toddler!",
                fontSize = (18 * fontScale).sp,
                fontFamily = fontFamily,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Learning is Fun!",
                fontSize = (16 * fontScale).sp,
                fontFamily = fontFamily,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
        }
    }
}
