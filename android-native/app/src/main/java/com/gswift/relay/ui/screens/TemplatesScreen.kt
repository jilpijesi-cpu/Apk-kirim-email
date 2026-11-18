package com.gswift.relay.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gswift.relay.data.model.EmailTemplate
import com.gswift.relay.ui.theme.NavyPrimary
import com.gswift.relay.ui.theme.RoseError
import com.gswift.relay.ui.viewmodel.MainUiState

@Composable
fun TemplatesScreen(
    uiState: MainUiState,
    onSaveTemplate: (name: String, category: String, subject: String, message: String, editingId: String?) -> Unit,
    onDeleteTemplate: (String) -> Unit,
    onApplyTemplate: (EmailTemplate) -> Unit,
    onSendTest: (String, EmailTemplate) -> Unit
) {
    var showEditorDialog by remember { mutableStateOf(false) }
    var editingTemplate by remember { mutableStateOf<EmailTemplate?>(null) }
    var testTargetTemplate by remember { mutableStateOf<EmailTemplate?>(null) }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingTemplate = null
                    showEditorDialog = true
                },
                containerColor = NavyPrimary,
                contentColor = Color.White
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Tambah Template")
            }
        }
    ) { padding ->
        if (uiState.templates.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Belum Ada Template Tersimpan",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Tekan tombol + untuk membuat template email baru yang tersimpan lokal di perangkat Anda.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(uiState.templates, key = { it.id }) { tpl ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = tpl.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        text = tpl.category.uppercase(),
                                        fontSize = 10.sp,
                                        color = NavyPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                IconButton(onClick = { onDeleteTemplate(tpl.id) }) {
                                    Icon(Icons.Rounded.Delete, contentDescription = "Hapus", tint = RoseError)
                                }
                            }
                            Text(
                                text = "Subjek: ${tpl.subject}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
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
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                                ) {
                                    Icon(Icons.Rounded.Send, contentDescription = null)
                                    Spacer(Modifier.width(4.dp))
                                    Text("Gunakan", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

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
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Kategori") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = subject,
                        onValueChange = { subject = it },
                        label = { Text("Subjek Email") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = message,
                        onValueChange = { message = it },
                        label = { Text("Konten / HTML") },
                        modifier = Modifier.height(120.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    enabled = name.isNotBlank() && subject.isNotBlank(),
                    onClick = {
                        onSaveTemplate(name.trim(), category.trim(), subject.trim(), message, editingTemplate?.id)
                        showEditorDialog = false
                    }
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

    testTargetTemplate?.let { tpl ->
        var emailInput by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { testTargetTemplate = null },
            title = { Text("Kirim Email Percobaan") },
            text = {
                OutlinedTextField(
                    value = emailInput,
                    onValueChange = { emailInput = it },
                    label = { Text("Alamat Email Tujuan") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    enabled = emailInput.contains("@"),
                    onClick = {
                        onSendTest(emailInput.trim(), tpl)
                        testTargetTemplate = null
                    }
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
}
