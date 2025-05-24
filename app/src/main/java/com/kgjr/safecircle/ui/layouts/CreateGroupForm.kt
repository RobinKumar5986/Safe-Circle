package com.kgjr.safecircle.ui.layouts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
@Composable
fun CreateGroupForm(
    groupName: String,
    groupDescription: String,
    adminEmail: String,
    onGroupNameChange: (String) -> Unit,
    onGroupDescriptionChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onCancel: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        tonalElevation = 4.dp,
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Create Group", style = MaterialTheme.typography.titleLarge, color = Color.Black)

            OutlinedTextField(
                value = groupName,
                onValueChange = onGroupNameChange,
                label = { Text("Group Name", color = Color.Gray) },
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.Black),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = groupDescription,
                onValueChange = onGroupDescriptionChange,
                label = { Text("Group Description", color = Color.Gray) },
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.Black),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = adminEmail,
                onValueChange = {},
                label = { Text("Admin Email", color = Color.Black) },
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.Gray),
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                enabled = true
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onCancel) {
                    Text("Cancel", color = Color.Black)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = onSubmit) {
                    Text("Create")
                }
            }
        }
    }
}
