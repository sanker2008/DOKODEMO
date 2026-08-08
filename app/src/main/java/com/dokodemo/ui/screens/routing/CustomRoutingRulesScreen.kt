package com.dokodemo.ui.screens.routing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.ContentPaste
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dokodemo.R
import com.dokodemo.data.preferences.AppPreferences
import com.dokodemo.data.preferences.CustomRoutingRule
import com.dokodemo.data.preferences.CustomRuleAction
import com.dokodemo.data.preferences.CustomRuleMatchType
import com.dokodemo.ui.components.DokoCard
import com.dokodemo.ui.components.DokoToggle
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CustomRoutingRulesUiState(
    val rules: List<CustomRoutingRule> = emptyList()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomRoutingRulesScreen(
    onNavigateBack: () -> Unit,
    viewModel: CustomRoutingRulesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var editingRule by remember { mutableStateOf<CustomRoutingRule?>(null) }
    var creatingRule by remember { mutableStateOf(false) }
    var batchImporting by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.custom_routing_rules), fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    IconButton(onClick = { batchImporting = true }) {
                        Icon(Icons.Rounded.ContentPaste, contentDescription = stringResource(R.string.batch_import))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { creatingRule = true }) {
                Icon(Icons.Rounded.Add, contentDescription = stringResource(R.string.custom_routing_rules_add))
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 96.dp, top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                OverviewCard(
                    totalCount = uiState.rules.size,
                    enabledCount = uiState.rules.count { it.enabled }
                )
            }
            item {
                SectionLabel(title = stringResource(R.string.custom_routing_rules_list))
            }
            if (uiState.rules.isEmpty()) {
                item {
                    EmptyRulesCard()
                }
            } else {
                item {
                    Text(
                        text = stringResource(R.string.custom_routing_rules_order_hint),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                itemsIndexed(uiState.rules, key = { _, rule -> rule.id }) { index, rule ->
                    RuleCard(
                        index = index,
                        rule = rule,
                        canMoveUp = index > 0,
                        canMoveDown = index < uiState.rules.lastIndex,
                        onToggleEnabled = { viewModel.toggleRule(rule.id) },
                        onEdit = { editingRule = rule },
                        onDelete = { viewModel.deleteRule(rule.id) },
                        onMoveUp = { viewModel.moveRule(rule.id, -1) },
                        onMoveDown = { viewModel.moveRule(rule.id, 1) }
                    )
                }
            }
        }
    }

    if (creatingRule) {
        RuleEditorDialog(
            initialRule = null,
            onDismiss = { creatingRule = false },
            onSave = {
                viewModel.upsertRule(it)
                creatingRule = false
            }
        )
    }

    editingRule?.let { rule ->
        RuleEditorDialog(
            initialRule = rule,
            onDismiss = { editingRule = null },
            onSave = {
                viewModel.upsertRule(it)
                editingRule = null
            }
        )
    }

    if (batchImporting) {
        BatchImportDialog(
            onDismiss = { batchImporting = false },
            onImport = { rules ->
                viewModel.addRules(rules)
                batchImporting = false
            }
        )
    }
}

@Composable
private fun SectionLabel(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp, start = 4.dp)
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun OverviewCard(
    totalCount: Int,
    enabledCount: Int
) {
    DokoCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = stringResource(R.string.custom_routing_rules_overview),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = stringResource(R.string.custom_routing_rules_empty_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RulePill(label = stringResource(R.string.custom_routing_rules_total_count, totalCount))
                RulePill(
                    label = stringResource(R.string.custom_routing_rules_enabled_count, enabledCount),
                    highlighted = true
                )
            }
        }
    }
}

@Composable
private fun EmptyRulesCard() {
    DokoCard {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 28.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = stringResource(R.string.custom_routing_rules_empty_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = stringResource(R.string.custom_routing_rules_order_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RuleCard(
    index: Int,
    rule: CustomRoutingRule,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onToggleEnabled: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit
) {
    DokoCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.custom_routing_rules_priority, index + 1),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = rule.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Switch(
                    checked = rule.enabled,
                    onCheckedChange = { onToggleEnabled() }
                )
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RulePill(label = rule.matchType.displayName)
                RulePill(label = rule.action.displayName, highlighted = true)
            }
            Text(
                text = rule.value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row {
                    IconButton(onClick = onMoveUp, enabled = canMoveUp) {
                        Icon(Icons.Rounded.ArrowUpward, contentDescription = stringResource(R.string.custom_routing_rules_move_up))
                    }
                    IconButton(onClick = onMoveDown, enabled = canMoveDown) {
                        Icon(Icons.Rounded.ArrowDownward, contentDescription = stringResource(R.string.custom_routing_rules_move_down))
                    }
                }
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Rounded.Edit, contentDescription = stringResource(R.string.custom_routing_rules_edit))
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Rounded.DeleteOutline, contentDescription = stringResource(R.string.delete))
                    }
                }
            }
        }
    }
}

@Composable
private fun RulePill(
    label: String,
    highlighted: Boolean = false
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = if (highlighted) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surface
        }
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = if (highlighted) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RuleEditorDialog(
    initialRule: CustomRoutingRule?,
    onDismiss: () -> Unit,
    onSave: (CustomRoutingRule) -> Unit
) {
    var name by remember(initialRule) { mutableStateOf(initialRule?.name.orEmpty()) }
    var value by remember(initialRule) { mutableStateOf(initialRule?.value.orEmpty()) }
    var matchType by remember(initialRule) { mutableStateOf(initialRule?.matchType ?: CustomRuleMatchType.DOMAIN_SUFFIX) }
    var action by remember(initialRule) { mutableStateOf(initialRule?.action ?: CustomRuleAction.PROXY) }
    var enabled by remember(initialRule) { mutableStateOf(initialRule?.enabled ?: true) }
    var typeExpanded by remember { mutableStateOf(false) }
    val canSave = value.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.background,
        title = {
            Text(
                text = stringResource(
                    if (initialRule == null) {
                        R.string.custom_routing_rules_add
                    } else {
                        R.string.custom_routing_rules_edit
                    }
                ),
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text(stringResource(R.string.custom_routing_rules_name)) },
                    placeholder = { Text(stringResource(R.string.custom_routing_rules_name_placeholder)) }
                )
                ExposedDropdownMenuBox(
                    expanded = typeExpanded,
                    onExpandedChange = { typeExpanded = !typeExpanded }
                ) {
                    OutlinedTextField(
                        value = matchType.displayName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.custom_routing_rules_match_type)) },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded)
                        },
                        modifier = Modifier
                            .menuAnchor(androidx.compose.material3.MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )
                    DropdownMenu(
                        expanded = typeExpanded,
                        onDismissRequest = { typeExpanded = false }
                    ) {
                        CustomRuleMatchType.entries.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item.displayName) },
                                onClick = {
                                    matchType = item
                                    typeExpanded = false
                                }
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = value,
                    onValueChange = { value = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.custom_routing_rules_value)) },
                    supportingText = {
                        Text(getValueHint(matchType))
                    }
                )
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    CustomRuleAction.entries.forEachIndexed { index, item ->
                        SegmentedButton(
                            selected = action == item,
                            onClick = { action = item },
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = CustomRuleAction.entries.size
                            ),
                            label = { Text(item.displayName) }
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(stringResource(R.string.custom_routing_rules_enabled), style = MaterialTheme.typography.bodyMedium)
                    DokoToggle(
                        checked = enabled,
                        onCheckedChange = { enabled = it }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val displayName = name.ifBlank { "${matchType.displayName}: ${value.trim()}" }
                    onSave(
                        CustomRoutingRule(
                            id = initialRule?.id ?: UUID.randomUUID().toString(),
                            name = displayName,
                            matchType = matchType,
                            value = value.trim(),
                            action = action,
                            enabled = enabled
                        )
                    )
                },
                enabled = canSave
            ) {
                Text(stringResource(R.string.custom_routing_rules_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
private fun getValueHint(matchType: CustomRuleMatchType): String {
    return when (matchType) {
        CustomRuleMatchType.DOMAIN_FULL -> stringResource(R.string.custom_rule_hint_domain_full)
        CustomRuleMatchType.DOMAIN_SUFFIX -> stringResource(R.string.custom_rule_hint_domain_suffix)
        CustomRuleMatchType.DOMAIN_KEYWORD -> stringResource(R.string.custom_rule_hint_domain_keyword)
        CustomRuleMatchType.IP_CIDR -> stringResource(R.string.custom_rule_hint_ip_cidr)
        CustomRuleMatchType.GEOSITE -> stringResource(R.string.custom_rule_hint_geosite)
        CustomRuleMatchType.GEOIP -> stringResource(R.string.custom_rule_hint_geoip)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BatchImportDialog(
    onDismiss: () -> Unit,
    onImport: (List<CustomRoutingRule>) -> Unit
) {
    var text by remember { mutableStateOf("") }
    var matchType by remember { mutableStateOf(CustomRuleMatchType.DOMAIN_SUFFIX) }
    var action by remember { mutableStateOf(CustomRuleAction.PROXY) }
    var typeExpanded by remember { mutableStateOf(false) }

    val lines = text.lines().map { it.trim() }.filter { it.isNotBlank() && !it.startsWith("#") }
    val validCount = lines.size

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.background,
        title = {
            Text(
                text = stringResource(R.string.batch_import),
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = stringResource(R.string.batch_import_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                ExposedDropdownMenuBox(
                    expanded = typeExpanded,
                    onExpandedChange = { typeExpanded = !typeExpanded }
                ) {
                    OutlinedTextField(
                        value = matchType.displayName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.batch_import_match_type)) },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded)
                        },
                        modifier = Modifier
                            .menuAnchor(androidx.compose.material3.MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )
                    DropdownMenu(
                        expanded = typeExpanded,
                        onDismissRequest = { typeExpanded = false }
                    ) {
                        CustomRuleMatchType.entries.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item.displayName) },
                                onClick = {
                                    matchType = item
                                    typeExpanded = false
                                }
                            )
                        }
                    }
                }
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    CustomRuleAction.entries.forEachIndexed { index, item ->
                        SegmentedButton(
                            selected = action == item,
                            onClick = { action = item },
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = CustomRuleAction.entries.size
                            ),
                            label = { Text(item.displayName) }
                        )
                    }
                }
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    label = { Text(stringResource(R.string.batch_import_values)) },
                    placeholder = { Text(stringResource(R.string.batch_import_placeholder)) },
                    supportingText = {
                        Text(
                            text = stringResource(R.string.batch_import_count, validCount),
                            color = if (validCount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val rules = lines.map { line ->
                        CustomRoutingRule(
                            id = UUID.randomUUID().toString(),
                            name = "${matchType.displayName}: $line",
                            matchType = matchType,
                            value = line,
                            action = action,
                            enabled = true
                        )
                    }
                    if (rules.isNotEmpty()) {
                        onImport(rules)
                    }
                },
                enabled = validCount > 0
            ) {
                Text(stringResource(R.string.batch_import_confirm, validCount))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@HiltViewModel
class CustomRoutingRulesViewModel @Inject constructor(
    private val appPreferences: AppPreferences
) : ViewModel() {
    private val _uiState = MutableStateFlow(CustomRoutingRulesUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            appPreferences.customRoutingRules.collect { rules ->
                _uiState.update { it.copy(rules = rules) }
            }
        }
    }

    fun upsertRule(rule: CustomRoutingRule) {
        viewModelScope.launch {
            val current = _uiState.value.rules.toMutableList()
            val index = current.indexOfFirst { it.id == rule.id }
            if (index >= 0) {
                current[index] = rule
            } else {
                current.add(rule)
            }
            appPreferences.setCustomRoutingRules(current)
        }
    }

    fun toggleRule(ruleId: String) {
        viewModelScope.launch {
            appPreferences.setCustomRoutingRules(
                _uiState.value.rules.map { rule ->
                    if (rule.id == ruleId) {
                        rule.copy(enabled = !rule.enabled)
                    } else {
                        rule
                    }
                }
            )
        }
    }

    fun deleteRule(ruleId: String) {
        viewModelScope.launch {
            appPreferences.setCustomRoutingRules(
                _uiState.value.rules.filterNot { it.id == ruleId }
            )
        }
    }

    fun moveRule(ruleId: String, delta: Int) {
        viewModelScope.launch {
            val current = _uiState.value.rules.toMutableList()
            val index = current.indexOfFirst { it.id == ruleId }
            val targetIndex = index + delta
            if (index == -1 || targetIndex !in current.indices) {
                return@launch
            }
            val item = current.removeAt(index)
            current.add(targetIndex, item)
            appPreferences.setCustomRoutingRules(current)
        }
    }

    fun addRules(rules: List<CustomRoutingRule>) {
        viewModelScope.launch {
            val current = _uiState.value.rules.toMutableList()
            current.addAll(rules)
            appPreferences.setCustomRoutingRules(current)
        }
    }
}
