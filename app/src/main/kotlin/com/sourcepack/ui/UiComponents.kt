package com.sourcepack.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.sourcepack.core.Str 

// 图标资源定义，避免导入庞大的 Material Icons 库
object Ico {
    private fun icon(name: String, block: ImageVector.Builder.() -> Unit): ImageVector {
        return ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f).apply(block).build()
    }
    private fun ImageVector.Builder.mPath(block: androidx.compose.ui.graphics.vector.PathBuilder.() -> Unit) {
        path(fill = SolidColor(Color.Black)) { block() }
    }

    val Settings = icon("Set") { mPath { moveTo(19.14f,12.94f);curveToRelative(0.04f,-0.3f,0.06f,-0.61f,0.06f,-0.94f);curveToRelative(0.0f,-0.32f,-0.02f,-0.64f,-0.06f,-0.94f);lineToRelative(2.03f,-1.58f);curveToRelative(0.18f,-0.14f,0.23f,-0.41f,0.12f,-0.61f);lineToRelative(-1.92f,-3.32f);curveToRelative(-0.12f,-0.22f,-0.37f,-0.29f,-0.59f,-0.22f);lineToRelative(-2.39f,0.96f);curveToRelative(-0.5f,-0.38f,-1.03f,-0.7f,-1.62f,-0.94f);lineToRelative(-0.36f,-2.54f);curveToRelative(-0.04f,-0.24f,-0.24f,-0.41f,-0.48f,-0.41f);horizontalLineToRelative(-3.84f);curveToRelative(-0.24f,0.0f,-0.43f,0.17f,-0.47f,0.41f);lineToRelative(-0.36f,2.54f);curveToRelative(-0.59f,0.24f,-1.13f,0.57f,-1.62f,0.94f);lineToRelative(-2.39f,-0.96f);curveToRelative(-0.22f,-0.08f,-0.47f,0.0f,-0.59f,0.22f);lineToRelative(-1.92f,3.32f);curveToRelative(-0.12f,0.21f,-0.07f,0.47f,0.12f,0.61f);lineToRelative(2.03f,1.58f);curveToRelative(-0.04f,0.3f,-0.06f,0.62f,-0.06f,0.94f);reflectiveCurveToRelative(0.02f,0.64f,0.06f,0.94f);lineToRelative(-2.03f,1.58f);curveToRelative(-0.18f,0.14f,-0.23f,0.41f,-0.12f,0.61f);lineToRelative(1.92f,3.32f);curveToRelative(0.12f,0.22f,0.37f,0.29f,0.59f,0.22f);lineToRelative(2.39f,-0.96f);curveToRelative(0.5f,0.38f,1.03f,0.7f,1.62f,0.94f);lineToRelative(0.36f,2.54f);curveToRelative(0.05f,0.24f,0.24f,0.41f,0.48f,0.41f);horizontalLineToRelative(3.84f);curveToRelative(0.24f,0.0f,0.44f,-0.17f,0.47f,-0.41f);lineToRelative(0.36f,-2.54f);curveToRelative(0.59f,-0.24f,1.13f,-0.56f,1.62f,-0.94f);lineToRelative(2.39f,0.96f);curveToRelative(0.22f,0.08f,0.47f,0.0f,0.59f,-0.22f);lineToRelative(1.92f,-3.32f);curveToRelative(0.12f,-0.22f,0.07f,-0.47f,-0.12f,-0.61f);lineToRelative(-2.01f,-1.58f);close();moveTo(12.0f,15.6f);curveToRelative(-1.98f,0.0f,-3.6f,-1.62f,-3.6f,-3.6f);reflectiveCurveToRelative(1.62f,-3.6f,3.6f,-3.6f);reflectiveCurveToRelative(3.6f,1.62f,3.6f,3.6f);reflectiveCurveToRelative(-1.62f,3.6f,-3.6f,3.6f);close() } }
    val Inventory2 = icon("Inv") { mPath { moveTo(20.0f, 2.0f); horizontalLineTo(4.0f); curveTo(2.9f, 2.0f, 2.0f, 2.9f, 2.0f, 4.0f); verticalLineToRelative(3.01f); curveTo(2.0f, 8.11f, 2.9f, 9.0f, 4.0f, 9.0f); horizontalLineToRelative(1.0f); verticalLineToRelative(11.0f); curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f); horizontalLineToRelative(10.0f); curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f); verticalLineTo(9.0f); horizontalLineToRelative(1.0f); curveToRelative(1.1f, 0.0f, 2.0f, -0.89f, 2.0f, -1.99f); verticalLineTo(4.0f); curveTo(22.0f, 2.9f, 21.1f, 2.0f, 20.0f, 2.0f); close(); moveTo(9.0f, 12.0f); horizontalLineToRelative(6.0f); verticalLineToRelative(2.0f); horizontalLineTo(9.0f); verticalLineTo(12.0f); close(); moveTo(20.0f, 7.0f); horizontalLineTo(4.0f); verticalLineTo(4.0f); horizontalLineToRelative(16.0f); verticalLineTo(7.0f); close() } }
    val Folder = icon("Fld") { mPath { moveTo(10.0f,4.0f);horizontalLineTo(4.0f);curveTo(2.9f,4.0f,2.01f,4.9f,2.01f,6.0f);lineTo(2.0f,18.0f);curveToRelative(0.0f,1.1f,0.9f,2.0f,2.0f,2.0f);horizontalLineToRelative(16.0f);curveToRelative(1.1f,0.0f,2.0f,-0.9f,2.0f,-2.0f);verticalLineTo(8.0f);curveToRelative(0.0f,-1.1f,-0.9f,-2.0f,-2.0f,-2.0f);horizontalLineToRelative(-8.0f);lineTo(10.0f,4.0f);close() } }
    val File = icon("File") { mPath { moveTo(14.0f,2.0f);horizontalLineTo(6.0f);curveTo(4.9f,2.0f,4.01f,2.9f,4.01f,4.0f);lineTo(4.0f,20.0f);curveToRelative(0.0f,1.1f,0.89f,2.0f,1.99f,2.0f);horizontalLineTo(18.0f);curveToRelative(1.1f,0.0f,2.0f,-0.9f,2.0f,-2.0f);verticalLineTo(8.0f);lineTo(14.0f,2.0f);close();moveTo(16.0f,18.0f);horizontalLineTo(8.0f);verticalLineToRelative(-2.0f);horizontalLineToRelative(8.0f);verticalLineTo(18.0f);close();moveTo(16.0f,14.0f);horizontalLineTo(8.0f);verticalLineToRelative(-2.0f);horizontalLineToRelative(8.0f);verticalLineTo(14.0f);close();moveTo(13.0f,9.0f);verticalLineTo(3.5f);lineTo(18.5f,9.0f);horizontalLineTo(13.0f);close() } }
    val CloudDownload = icon("CloudDl") { mPath { moveTo(19.35f,10.04f);curveTo(18.67f,6.59f,15.64f,4.0f,12.0f,4.0f);curveTo(9.11f,4.0f,6.6f,5.64f,5.35f,8.04f);curveTo(2.34f,8.36f,0.0f,10.91f,0.0f,14.0f);curveToRelative(0.0f,3.31f,2.69f,6.0f,6.0f,6.0f);horizontalLineToRelative(13.0f);curveToRelative(2.76f,0.0f,5.0f,-2.24f,5.0f,-5.0f);curveToRelative(0.0f,-2.64f,-2.05f,-4.78f,-4.65f,-4.96f);close();moveTo(17.0f,13.0f);lineToRelative(-5.0f,5.0f);lineToRelative(-5.0f,-5.0f);horizontalLineToRelative(3.0f);verticalLineTo(9.0f);horizontalLineToRelative(4.0f);verticalLineToRelative(4.0f);horizontalLineToRelative(3.0f);close() } }
    val ArrowBack = icon("Back") { mPath { moveTo(20.0f,11.0f);horizontalLineTo(7.83f);lineToRelative(5.59f,-5.59f);lineTo(12.0f,4.0f);lineToRelative(-8.0f,8.0f);lineToRelative(8.0f,8.0f);lineToRelative(1.41f,-1.41f);lineTo(7.83f,13.0f);horizontalLineTo(20.0f);verticalLineToRelative(-2.0f);close() } }
    val ArrowRight = icon("Right") { mPath { moveTo(10.0f,6.0f);lineToRelative(8.57f,6.0f);lineTo(10.0f,18.0f);close() } }
    val Add = icon("Add") { mPath { moveTo(19.0f,13.0f);horizontalLineToRelative(-6.0f);verticalLineToRelative(6.0f);horizontalLineToRelative(-2.0f);verticalLineToRelative(-6.0f);horizontalLineTo(5.0f);verticalLineToRelative(-2.0f);horizontalLineToRelative(6.0f);verticalLineTo(5.0f);horizontalLineToRelative(2.0f);verticalLineToRelative(6.0f);horizontalLineToRelative(6.0f);verticalLineToRelative(2.0f);close() } }
    val Delete = icon("Del") { mPath { moveTo(6.0f,19.0f);curveToRelative(0.0f,1.1f,0.9f,2.0f,2.0f,2.0f);horizontalLineToRelative(8.0f);curveToRelative(1.1f,0.0f,2.0f,-0.9f,2.0f,-2.0f);verticalLineTo(7.0f);horizontalLineTo(6.0f);verticalLineToRelative(12.0f);close();moveTo(19.0f,4.0f);horizontalLineToRelative(-3.5f);lineToRelative(-1.0f,-1.0f);horizontalLineToRelative(-5.0f);lineToRelative(-1.0f,1.0f);horizontalLineTo(5.0f);verticalLineToRelative(2.0f);horizontalLineToRelative(14.0f);verticalLineTo(4.0f);close() } }
    val Check = icon("Check") { mPath { moveTo(9.0f,16.17f);lineTo(4.83f,12.0f);lineToRelative(-1.42f,1.41f);lineTo(9.0f,19.0f);lineTo(21.0f,7.0f);lineToRelative(-1.41f,-1.41f);lineTo(9.0f,16.17f);close() } }
    val CheckCircle = icon("Ok") { mPath { moveTo(12.0f, 2.0f); curveTo(6.48f, 2.0f, 2.0f, 6.48f, 2.0f, 12.0f); reflectiveCurveToRelative(4.48f, 10.0f, 10.0f, 10.0f); reflectiveCurveToRelative(10.0f, -4.48f, 10.0f, -10.0f); reflectiveCurveTo(17.52f, 2.0f, 12.0f, 2.0f); close(); moveTo(10.0f, 17.0f); lineToRelative(-5.0f, -5.0f); lineToRelative(1.41f, -1.41f); lineTo(10.0f, 14.17f); lineToRelative(7.59f, -7.59f); lineTo(19.0f, 8.0f); lineTo(10.0f, 17.0f); close() } }
    val Error = icon("Err") { mPath { moveTo(12.0f, 2.0f); curveTo(6.48f, 2.0f, 2.0f, 6.48f, 2.0f, 12.0f); reflectiveCurveToRelative(4.48f, 10.0f, 10.0f, 10.0f); reflectiveCurveToRelative(10.0f, -4.48f, 10.0f, -10.0f); reflectiveCurveTo(17.52f, 2.0f, 12.0f, 2.0f); close(); moveTo(13.0f, 17.0f); horizontalLineToRelative(-2.0f); verticalLineToRelative(-2.0f); horizontalLineToRelative(2.0f); verticalLineToRelative(2.0f); close(); moveTo(13.0f, 13.0f); horizontalLineToRelative(-2.0f); verticalLineTo(7.0f); horizontalLineToRelative(2.0f); verticalLineToRelative(6.0f); close() } }
    val Palette = icon("Palette") { mPath { moveTo(12.0f,3.0f);curveToRelative(-4.97f,0.0f,-9.0f,4.03f,-9.0f,9.0f);reflectiveCurveToRelative(4.03f,9.0f,9.0f,9.0f);curveToRelative(0.83f,0.0f,1.5f,-0.67f,1.5f,-1.5f);curveToRelative(0.0f,-0.39f,-0.15f,-0.74f,-0.39f,-1.01f);curveToRelative(-0.23f,-0.26f,-0.38f,-0.61f,-0.38f,-0.99f);curveToRelative(0.0f,-0.83f,0.67f,-1.5f,1.5f,-1.5f);horizontalLineTo(16.0f);curveToRelative(2.76f,0.0f,5.0f,-2.24f,5.0f,-5.0f);curveToRelative(0.0f,-4.42f,-4.03f,-8.0f,-9.0f,-8.0f);close();moveTo(6.5f,12.0f);curveToRelative(-0.83f,0.0f,-1.5f,-0.67f,-1.5f,-1.5f);reflectiveCurveTo(5.67f,9.0f,6.5f,9.0f);reflectiveCurveToRelative(1.5f,0.67f,1.5f,1.5f);reflectiveCurveTo(7.33f,12.0f,6.5f,12.0f);close();moveTo(9.5f,8.0f);curveTo(8.67f,8.0f,8.0f,7.33f,8.0f,6.5f);reflectiveCurveTo(8.67f,5.0f,9.5f,5.0f);reflectiveCurveTo(11.0f,5.67f,11.0f,6.5f);reflectiveCurveTo(10.33f,8.0f,9.5f,8.0f);close();moveTo(14.5f,8.0f);curveToRelative(-0.83f,0.0f,-1.5f,-0.67f,-1.5f,-1.5f);reflectiveCurveTo(13.67f,5.0f,14.5f,5.0f);reflectiveCurveToRelative(1.5f,0.67f,1.5f,1.5f);reflectiveCurveTo(15.33f,8.0f,14.5f,8.0f);close();moveTo(17.5f,12.0f);curveToRelative(-0.83f,0.0f,-1.5f,-0.67f,-1.5f,-1.5f);reflectiveCurveTo(16.67f,9.0f,17.5f,9.0f);reflectiveCurveToRelative(1.5f,0.67f,1.5f,1.5f);reflectiveCurveTo(18.33f,12.0f,17.5f,12.0f);close() } }
    val Description = icon("Desc") { mPath { moveTo(14.0f,2.0f);horizontalLineTo(6.0f);curveTo(4.9f,2.0f,4.01f,2.9f,4.01f,4.0f);lineTo(4.0f,20.0f);curveToRelative(0.0f,1.1f,0.89f,2.0f,1.99f,2.0f);horizontalLineTo(18.0f);curveToRelative(1.1f,0.0f,2.0f,-0.9f,2.0f,-2.0f);verticalLineTo(8.0f);lineTo(14.0f,2.0f);close();moveTo(16.0f,18.0f);horizontalLineTo(8.0f);verticalLineToRelative(-2.0f);horizontalLineToRelative(8.0f);verticalLineTo(18.0f);close();moveTo(16.0f,14.0f);horizontalLineTo(8.0f);verticalLineToRelative(-2.0f);horizontalLineToRelative(8.0f);verticalLineTo(14.0f);close();moveTo(13.0f,9.0f);verticalLineTo(3.5f);lineTo(18.5f,9.0f);horizontalLineTo(13.0f);close() } }
    val Info = icon("Info") { mPath { moveTo(12.0f, 2.0f); curveTo(6.48f, 2.0f, 2.0f, 6.48f, 2.0f, 12.0f); reflectiveCurveToRelative(4.48f, 10.0f, 10.0f, 10.0f); reflectiveCurveToRelative(10.0f, -4.48f, 10.0f, -10.0f); reflectiveCurveTo(17.52f, 2.0f, 12.0f, 2.0f); close(); moveTo(13.0f, 17.0f); horizontalLineToRelative(-2.0f); verticalLineToRelative(-6.0f); horizontalLineToRelative(2.0f); verticalLineToRelative(6.0f); close(); moveTo(13.0f, 9.0f); horizontalLineToRelative(-2.0f); verticalLineTo(7.0f); horizontalLineToRelative(2.0f); verticalLineToRelative(2.0f); close() } }
    val Copyright = icon("Copy") { mPath { moveTo(11.7f, 2.0f); curveTo(6.4f, 2.0f, 2.0f, 6.4f, 2.0f, 11.7f); reflectiveCurveToRelative(4.4f, 9.7f, 9.7f, 9.7f); reflectiveCurveToRelative(9.7f, -4.4f, 9.7f, -9.7f); reflectiveCurveTo(17.0f, 2.0f, 11.7f, 2.0f); close(); moveTo(11.7f, 19.5f); curveToRelative(-4.3f, 0.0f, -7.8f, -3.5f, -7.8f, -7.8f); reflectiveCurveToRelative(3.5f, -7.8f, 7.8f, -7.8f); reflectiveCurveToRelative(7.8f, 3.5f, 7.8f, 7.8f); reflectiveCurveToRelative(-3.5f, 7.8f, -7.8f, 7.8f); close(); moveTo(11.7f, 8.2f); curveToRelative(-2.0f, 0.0f, -3.5f, 1.5f, -3.5f, 3.5f); reflectiveCurveToRelative(1.5f, 3.5f, 3.5f, 3.5f); curveToRelative(1.0f, 0.0f, 1.8f, -0.4f, 2.4f, -1.0f); lineToRelative(1.1f, 1.1f); curveToRelative(-0.9f, 0.9f, -2.1f, 1.4f, -3.5f, 1.4f); curveToRelative(-2.8f, 0.0f, -5.0f, -2.2f, -5.0f, -5.0f); reflectiveCurveToRelative(2.2f, -5.0f, 5.0f, -5.0f); curveToRelative(1.4f, 0.0f, 2.6f, 0.5f, 3.5f, 1.4f); lineToRelative(-1.1f, 1.1f); curveToRelative(-0.6f, -0.6f, -1.4f, -1.0f, -2.4f, -1.0f); close() } }
    val SelectAll = icon("SelAll") { mPath { moveTo(18.0f,7.0f);lineToRelative(-2.0f,0.0f);lineToRelative(0.0f,-2.0f);lineToRelative(2.0f,0.0f);close();moveTo(18.0f,9.0f);lineToRelative(-2.0f,0.0f);lineToRelative(0.0f,2.0f);lineToRelative(2.0f,0.0f);close();moveTo(18.0f,13.0f);lineToRelative(-2.0f,0.0f);lineToRelative(0.0f,2.0f);lineToRelative(2.0f,0.0f);close();moveTo(22.0f,7.0f);lineToRelative(-2.0f,0.0f);lineToRelative(0.0f,-2.0f);lineToRelative(2.0f,0.0f);close();moveTo(22.0f,9.0f);lineToRelative(-2.0f,0.0f);lineToRelative(0.0f,2.0f);lineToRelative(2.0f,0.0f);close();moveTo(22.0f,13.0f);lineToRelative(-2.0f,0.0f);lineToRelative(0.0f,2.0f);lineToRelative(2.0f,0.0f);close();moveTo(14.0f,7.0f);lineToRelative(-2.0f,0.0f);lineToRelative(0.0f,-2.0f);lineToRelative(2.0f,0.0f);close();moveTo(22.0f,18.0f);lineToRelative(-0.0f,-2.0f);lineToRelative(-2.0f,0.0f);lineToRelative(0.0f,2.0f);lineToRelative(-2.0f,0.0f);lineToRelative(0.0f,2.0f);lineToRelative(2.0f,0.0f);lineToRelative(0.0f,2.0f);lineToRelative(2.0f,0.0f);lineToRelative(0.0f,-2.0f);lineToRelative(2.0f,0.0f);lineToRelative(0.0f,-2.0f);close();moveTo(8.0f,18.0f);lineToRelative(0.0f,-2.0f);lineToRelative(-2.0f,0.0f);lineToRelative(0.0f,2.0f);close();moveTo(12.0f,18.0f);lineToRelative(0.0f,-2.0f);lineToRelative(-2.0f,0.0f);lineToRelative(0.0f,2.0f);close();moveTo(4.0f,18.0f);lineToRelative(0.0f,-2.0f);lineToRelative(-2.0f,0.0f);lineToRelative(0.0f,2.0f);close();moveTo(4.0f,14.0f);lineToRelative(0.0f,-2.0f);lineToRelative(-2.0f,0.0f);lineToRelative(0.0f,2.0f);close();moveTo(4.0f,10.0f);lineToRelative(0.0f,-2.0f);lineToRelative(-2.0f,0.0f);lineToRelative(0.0f,2.0f);close();moveTo(4.0f,6.0f);lineToRelative(0.0f,-2.0f);lineToRelative(-2.0f,0.0f);lineToRelative(0.0f,2.0f);close();moveTo(6.0f,2.0f);lineToRelative(0.0f,2.0f);lineToRelative(2.0f,0.0f);lineToRelative(0.0f,-2.0f);close();moveTo(10.0f,2.0f);lineToRelative(0.0f,2.0f);lineToRelative(2.0f,0.0f);lineToRelative(0.0f,-2.0f);close() } }
    val UnselectAll = icon("UnSel") { mPath { moveTo(2.0f,2.0f);lineToRelative(20.0f,20.0f);lineToRelative(-1.41f,1.41f);lineToRelative(-2.0f,-2.0f);lineTo(18.0f,22.0f);lineTo(6.0f,22.0f);curveToRelative(-1.1f,0.0f,-2.0f,-0.9f,-2.0f,-2.0f);lineTo(4.0f,8.0f);lineToRelative(-2.0f,-2.0f);lineTo(2.0f,2.0f);close();moveTo(18.0f,16.0f);lineToRelative(-2.0f,-2.0f);verticalLineToRelative(-2.0f);horizontalLineToRelative(-2.0f);lineTo(12.17f,10.17f);lineTo(13.83f,11.83f);lineTo(14.0f,12.0f);verticalLineToRelative(2.0f);horizontalLineToRelative(2.0f);verticalLineToRelative(2.0f);close() } }
}

@Composable
fun HomeBtn(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
            Spacer(Modifier.width(16.dp))
            Column {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun ResultCard(success: Boolean, msg: String, onShare: (() -> Unit)?, onReset: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (success) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer
        ),
        modifier = Modifier.padding(16.dp).fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                if (success) Ico.CheckCircle else Ico.Error,
                null,
                modifier = Modifier.size(48.dp),
                tint = if (success) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
            Spacer(Modifier.height(16.dp))
            Text(msg, style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(24.dp))
            Row {
                Button(onClick = onReset) { Text("OK") }
                if (success && onShare != null) {
                    Spacer(Modifier.width(16.dp))
                    FilledTonalButton(onClick = onShare) { 
                        Text(Str.get("分享", "Share")) 
                    }
                }
            }
        }
    }
}

@Composable
fun SettingHeader(text: String) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.labelLarge,
        modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp)
    )
}

@Composable
fun SettingLink(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        leadingContent = { Icon(icon, null) },
        trailingContent = { Icon(Ico.ArrowRight, null) }
    )
}

@Composable
fun SwitchItem(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    ListItem(
        headlineContent = { Text(title) },
        trailingContent = { Switch(checked = checked, onCheckedChange = onCheckedChange) }
    )
}

@Composable
fun LoadingView(msg: String, detail: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(24.dp)
    ) {
        CircularProgressIndicator(modifier = Modifier.size(48.dp))
        Spacer(Modifier.height(24.dp))
        Text(msg, style = MaterialTheme.typography.titleMedium)
        if (detail.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Text(
                detail, 
                style = MaterialTheme.typography.bodySmall, 
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
    }
}