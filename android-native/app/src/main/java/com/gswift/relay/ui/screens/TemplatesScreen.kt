package com.gswift.relay.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.gswift.relay.data.model.EmailTemplate
import com.gswift.relay.ui.components.NativeReceiptPreview
import com.gswift.relay.ui.theme.NavyPrimary
import com.gswift.relay.ui.theme.RoseError
import com.gswift.relay.ui.viewmodel.MainUiState

@Composable
fun TemplatesScreen(
    uiState: MainUiState,
    onSaveTemplate: (name: String, category: String, subject: String, message: String, editingId: String?) -> Unit,
    onDeleteTemplate: (String) -> Unit,
    onDeleteAllTemplates: () -> Unit,
    onApplyTemplate: (EmailTemplate) -> Unit,
    onSendTest: (String, EmailTemplate) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var showEditorDialog by remember { mutableStateOf(false) }
    var editingTemplate by remember { mutableStateOf<EmailTemplate?>(null) }
    var testTargetTemplate by remember { mutableStateOf<EmailTemplate?>(null) }
    var previewTemplate by remember { mutableStateOf<EmailTemplate?>(null) }
    var confirmDeleteTemplate by remember { mutableStateOf<EmailTemplate?>(null) }
    var confirmDeleteAll by remember { mutableStateOf(false) }

    val filteredTemplates = remember(uiState.templates, searchQuery) {
        if (searchQuery.isBlank()) {
            uiState.templates
        } else {
            val q = searchQuery.trim().lowercase()
            uiState.templates.filter {
                it.name.lowercase().contains(q) ||
                    it.subject.lowercase().contains(q) ||
                    it.category.lowercase().contains(q)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp, vertical = 14.dp)
    ) {
        // Header matching Screenshot 2 (Templates Email)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Templates Email",
                    fontSize = 21.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF0F172A)
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "Gunakan template untuk kirim cepat",
                    fontSize = 13.sp,
                    color = Color(0xFF64748B)
                )
            }

            if (uiState.templates.isNotEmpty()) {
                OutlinedButton(
                    onClick = { confirmDeleteAll = true },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFFDA4AF)),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(Icons.Rounded.DeleteSweep, contentDescription = null, tint = RoseError, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Hapus Semua", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = RoseError)
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        // Search Box + Dark Blue "+" Square Button matching Screenshot 2
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        "Cari template...",
                        fontSize = 13.sp,
                        color = Color(0xFF94A3B8)
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Rounded.Search,
                        contentDescription = null,
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                Icons.Rounded.Close,
                                contentDescription = "Hapus Pencarian",
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = NavyPrimary,
                    unfocusedBorderColor = Color(0xFFE2E8F0)
                ),
                modifier = Modifier.weight(1f)
            )

            Box(
                modifier = Modifier
                    .size(52.dp)
                    .shadow(4.dp, RoundedCornerShape(15.dp))
                    .clip(RoundedCornerShape(15.dp))
                    .background(NavyPrimary)
                    .clickable {
                        editingTemplate = null
                        showEditorDialog = true
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = "Tambah Template",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        if (filteredTemplates.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(horizontal = 24.dp)
                ) {
                    Surface(
                        color = Color.White.copy(alpha = 0.92f),
                        shape = CircleShape,
                        shadowElevation = 3.dp,
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.size(76.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.Description,
                                contentDescription = null,
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(34.dp)
                            )
                        }
                    }

                    Text(
                        text = if (uiState.templates.isEmpty()) {
                            "Belum Ada Template Tersimpan"
                        } else {
                            "Template Tidak Ditemukan"
                        },
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF0F172A),
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = if (uiState.templates.isEmpty()) {
                            "Mulai dengan membuat template email baru menggunakan tombol \"+\" di atas"
                        } else {
                            "Coba kata kunci lain atau buat template email baru menggunakan tombol \"+\"."
                        },
                        fontSize = 13.5.sp,
                        lineHeight = 19.sp,
                        color = Color(0xFF64748B),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            Spacer(Modifier.height(14.dp))
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredTemplates, key = { it.id }) { tpl ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(18.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(18.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Surface(
                                        color = Color(0xFFEFF6FF),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = tpl.category.uppercase(),
                                            fontSize = 9.5.sp,
                                            color = NavyPrimary,
                                            fontWeight = FontWeight.ExtraBold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = tpl.name,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 15.sp,
                                        color = Color(0xFF0F172A),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Row {
                                    IconButton(onClick = { previewTemplate = tpl }) {
                                        Icon(Icons.Rounded.Visibility, contentDescription = "Pratinjau", tint = NavyPrimary)
                                    }
                                    IconButton(onClick = {
                                        editingTemplate = tpl
                                        showEditorDialog = true
                                    }) {
                                        Icon(Icons.Rounded.Edit, contentDescription = "Edit", tint = Color(0xFF475569))
                                    }
                                    IconButton(onClick = { confirmDeleteTemplate = tpl }) {
                                        Icon(Icons.Rounded.Delete, contentDescription = "Hapus", tint = RoseError)
                                    }
                                }
                            }
                            Text(
                                text = "Subjek: ${tpl.subject}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { testTargetTemplate = tpl },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Kirim Tes", fontSize = 12.sp)
                                }
                                Button(
                                    onClick = { onApplyTemplate(tpl) },
                                    modifier = Modifier.weight(1.2f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                                ) {
                                    Icon(Icons.Rounded.Send, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.width(5.dp))
                                    Text("PAKAI TEMPLATE", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Create / Edit Template Dialog
    if (showEditorDialog) {
        var name by remember { mutableStateOf(editingTemplate?.name ?: "") }
        var category by remember { mutableStateOf(editingTemplate?.category ?: "Umum") }
        var subject by remember { mutableStateOf(editingTemplate?.subject ?: "") }
        var message by remember { mutableStateOf(editingTemplate?.message ?: "") }

        AlertDialog(
            onDismissRequest = { showEditorDialog = false },
            title = { Text(if (editingTemplate == null) "Template Baru" else "Edit Template") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nama Template") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Kategori") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = subject,
                        onValueChange = { subject = it },
                        label = { Text("Subjek Email") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = message,
                        onValueChange = { message = it },
                        label = { Text("Konten / HTML") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    enabled = name.isNotBlank() && subject.isNotBlank(),
                    onClick = {
                        onSaveTemplate(name.trim(), category.trim(), subject.trim(), message, editingTemplate?.id)
                        showEditorDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                ) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditorDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    // Preview Template Modal (Eye icon)
    previewTemplate?.let { tpl ->
        Dialog(onDismissRequest = { previewTemplate = null }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = tpl.name,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "Subjek: ${tpl.subject}",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        IconButton(onClick = { previewTemplate = null }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Rounded.Close, contentDescription = "Tutup", modifier = Modifier.size(18.dp))
                        }
                    }

                    NativeReceiptPreview(
                        html = tpl.message,
                        subject = tpl.subject,
                        compactMobileMode = true
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { previewTemplate = null },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Tutup")
                        }
                        Button(
                            onClick = {
                                previewTemplate = null
                                onApplyTemplate(tpl)
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                            modifier = Modifier.weight(1.2f)
                        ) {
                            Text("Pakai Template", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Send Test Email Dialog (pre-filled with configured senderEmail/username)
    testTargetTemplate?.let { tpl ->
        val defaultEmail = uiState.smtpConfig.senderEmail.ifBlank { uiState.smtpConfig.username }
        var emailInput by remember(tpl.id) { mutableStateOf(defaultEmail) }
        AlertDialog(
            onDismissRequest = { testTargetTemplate = null },
            title = { Text("Kirim Email Percobaan") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Template '${tpl.name}' akan dikirim ke alamat di bawah:",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        label = { Text("Alamat Email Tujuan") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    enabled = emailInput.contains("@"),
                    onClick = {
                        onSendTest(emailInput.trim(), tpl)
                        testTargetTemplate = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                ) {
                    Text("Kirim Tes")
                }
            },
            dismissButton = {
                TextButton(onClick = { testTargetTemplate = null }) {
                    Text("Batal")
                }
            }
        )
    }

    // Confirm Single Delete
    confirmDeleteTemplate?.let { tpl ->
        AlertDialog(
            onDismissRequest = { confirmDeleteTemplate = null },
            title = { Text("Hapus Template?") },
            text = { Text("Template '${tpl.name}' akan dihapus permanen.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteTemplate(tpl.id)
                        confirmDeleteTemplate = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoseError)
                ) {
                    Text("Hapus")
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDeleteTemplate = null }) {
                    Text("Batal")
                }
            }
        )
    }

    // Confirm Delete All
    if (confirmDeleteAll) {
        AlertDialog(
            onDismissRequest = { confirmDeleteAll = false },
            title = { Text("Hapus Semua Template?") },
            text = { Text("Seluruh ${uiState.templates.size} template tersimpan akan dihapus permanen.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteAllTemplates()
                        confirmDeleteAll = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoseError)
                ) {
                    Text("Hapus Semua")
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDeleteAll = false }) {
                    Text("Batal")
                }
            }
        )
    }
}
