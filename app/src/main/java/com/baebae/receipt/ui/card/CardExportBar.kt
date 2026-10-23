package com.baebae.receipt.ui.card

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.baebae.receipt.R
import com.baebae.receipt.export.CardImageExporter
import kotlinx.coroutines.launch

/** [ScaledCard]의 contentModifier로 넘겨 카드 그리기를 [layer]에 기록한다. */
fun Modifier.recordInto(layer: GraphicsLayer): Modifier = drawWithContent {
    layer.record { this@drawWithContent.drawContent() }
    drawLayer(layer)
}

@Composable
fun rememberCardLayer(): GraphicsLayer = rememberGraphicsLayer()

/** 예) nalabel_2026-W43_story */
fun exportFileName(card: com.baebae.receipt.domain.LabelCard, format: CardFormat): String =
    "nalabel_" + CardText.lot(card.periodStart).removePrefix("LOT ") + "_" + format.name.lowercase()

enum class ExportAction { SAVE, SHARE }

/**
 * 저장/공유 버튼. [beforeExport]가 false를 돌려주면 내보내지 않는다(예: 보상형 광고 대기).
 * [onExported]는 실제로 이미지가 만들어진 뒤 호출된다(워터마크 제거권 차감 등).
 */
@Composable
fun CardExportBar(
    layer: GraphicsLayer,
    fileName: String,
    modifier: Modifier = Modifier,
    onExported: (ExportAction) -> Unit = {},
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var busy by remember { mutableStateOf(false) }
    val folder = stringResource(R.string.gallery_folder)
    val chooserTitle = stringResource(R.string.share_chooser_title)
    val savedMessage = stringResource(R.string.export_saved)
    val failedMessage = stringResource(R.string.export_failed)

    fun run(action: ExportAction) {
        if (busy) return
        busy = true
        scope.launch {
            try {
                val bitmap = layer.toImageBitmap().asAndroidBitmap().toSoftware()
                when (action) {
                    ExportAction.SAVE -> {
                        val uri = CardImageExporter.saveToGallery(context, bitmap, fileName, folder)
                        Toast.makeText(context, if (uri != null) savedMessage else failedMessage, Toast.LENGTH_SHORT).show()
                        if (uri != null) onExported(action)
                    }
                    ExportAction.SHARE -> {
                        val uri = CardImageExporter.prepareShare(context, bitmap, fileName)
                        context.startActivity(CardImageExporter.shareIntent(uri, chooserTitle))
                        onExported(action)
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, failedMessage, Toast.LENGTH_SHORT).show()
            } finally {
                busy = false
            }
        }
    }

    val storagePermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) run(ExportAction.SAVE)
        else Toast.makeText(context, failedMessage, Toast.LENGTH_SHORT).show()
    }

    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(
            onClick = {
                if (needsLegacyStoragePermission(context)) {
                    storagePermission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                } else {
                    run(ExportAction.SAVE)
                }
            },
            enabled = !busy,
            shape = RoundedCornerShape(0.dp),
            modifier = Modifier.weight(1f),
        ) { Text(stringResource(R.string.action_save)) }
        Button(
            onClick = { run(ExportAction.SHARE) },
            enabled = !busy,
            shape = RoundedCornerShape(0.dp),
            modifier = Modifier.weight(1f),
        ) { Text(stringResource(R.string.action_share)) }
    }
}

private fun needsLegacyStoragePermission(context: Context): Boolean =
    Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
        ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
        PackageManager.PERMISSION_GRANTED

/** 하드웨어 비트맵은 PNG 압축 전에 소프트웨어 비트맵으로 복사한다. */
private fun Bitmap.toSoftware(): Bitmap =
    if (config == Bitmap.Config.HARDWARE) copy(Bitmap.Config.ARGB_8888, false) else this
