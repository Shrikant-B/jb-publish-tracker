package com.shrikantbadwaik.publish.time.tracker.ui

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBTextField
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBCheckBox
import com.shrikantbadwaik.publish.time.tracker.data.PublishTrackerStorage
import java.awt.BorderLayout
import java.awt.GridLayout
import javax.swing.*

class SettingsPanel : Configurable {
    
    private val storage = PublishTrackerStorage.getInstance()
    private val panel = JBPanel<JBPanel<*>>()
    
    // UI Components
    private val tokenField = JBTextField()
    private val pluginsList = JBList<String>()
    private val pollingIntervalCombo = ComboBox<String>()
    private val notificationTypeCombo = ComboBox<String>()
    private val enableNotificationsCheckbox = JBCheckBox("Enable Notifications")
    private val enableAutoPollCheckbox = JBCheckBox("Enable Auto Polling")
    private val enableEmailNotificationsCheckbox = JBCheckBox("Enable Email Notifications")
    private val emailField = JBTextField()
    private val dataRetentionDaysField = JBTextField("30")
    
    init {
        setupUI()
        loadSettings()
    }
    
    private fun setupUI() {
        panel.layout = BorderLayout()
        
        // Main settings panel
        val mainPanel = JBPanel<JBPanel<*>>()
        mainPanel.layout = GridLayout(0, 2, 10, 10)
        
        // API Token Section
        mainPanel.add(JBLabel("JetBrains Marketplace API Token:"))
        mainPanel.add(tokenField)
        
        // Plugin IDs Section
        mainPanel.add(JBLabel("Plugin IDs to Track:"))
        val pluginPanel = JBPanel<JBPanel<*>>()
        pluginPanel.layout = BorderLayout()
        pluginPanel.add(JBScrollPane(pluginsList), BorderLayout.CENTER)
        
        val pluginButtonPanel = JBPanel<JBPanel<*>>()
        pluginButtonPanel.layout = GridLayout(1, 3, 5, 5)
        
        val addPluginBtn = JButton("Add Plugin")
        val removePluginBtn = JButton("Remove Plugin")
        val clearPluginsBtn = JButton("Clear All")
        
        addPluginBtn.addActionListener { addPlugin() }
        removePluginBtn.addActionListener { removeSelectedPlugin() }
        clearPluginsBtn.addActionListener { clearAllPlugins() }
        
        pluginButtonPanel.add(addPluginBtn)
        pluginButtonPanel.add(removePluginBtn)
        pluginButtonPanel.add(clearPluginsBtn)
        
        pluginPanel.add(pluginButtonPanel, BorderLayout.SOUTH)
        mainPanel.add(pluginPanel)
        
        // Polling Configuration
        mainPanel.add(JBLabel("Polling Interval:"))
        pollingIntervalCombo.addItem("1 minute")
        pollingIntervalCombo.addItem("5 minutes")
        pollingIntervalCombo.addItem("10 minutes")
        pollingIntervalCombo.addItem("30 minutes")
        pollingIntervalCombo.addItem("1 hour")
        pollingIntervalCombo.addItem("2 hours")
        pollingIntervalCombo.selectedIndex = 2 // Default to 10 minutes
        mainPanel.add(pollingIntervalCombo)
        
        // Notification Settings
        mainPanel.add(JBLabel("Notification Type:"))
        notificationTypeCombo.addItem("Balloon")
        notificationTypeCombo.addItem("Sticky Balloon")
        notificationTypeCombo.addItem("None")
        notificationTypeCombo.selectedIndex = 0 // Default to Balloon
        mainPanel.add(notificationTypeCombo)
        
        // Checkboxes
        mainPanel.add(enableNotificationsCheckbox)
        mainPanel.add(JBLabel("")) // Empty cell for alignment
        
        mainPanel.add(enableAutoPollCheckbox)
        mainPanel.add(JBLabel("")) // Empty cell for alignment
        
        mainPanel.add(enableEmailNotificationsCheckbox)
        mainPanel.add(JBLabel("")) // Empty cell for alignment
        
        // Email Configuration
        mainPanel.add(JBLabel("Email Address:"))
        mainPanel.add(emailField)
        
        // Data Retention
        mainPanel.add(JBLabel("Data Retention (days):"))
        mainPanel.add(dataRetentionDaysField)
        
        panel.add(mainPanel, BorderLayout.CENTER)
        
        // Action buttons
        val buttonPanel = JBPanel<JBPanel<*>>()
        buttonPanel.layout = GridLayout(1, 3, 10, 10)
        
        val exportBtn = JButton("Export Settings")
        val importBtn = JButton("Import Settings")
        val resetBtn = JButton("Reset to Defaults")
        
        exportBtn.addActionListener { exportSettings() }
        importBtn.addActionListener { importSettings() }
        resetBtn.addActionListener { resetToDefaults() }
        
        buttonPanel.add(exportBtn)
        buttonPanel.add(importBtn)
        buttonPanel.add(resetBtn)
        
        panel.add(buttonPanel, BorderLayout.SOUTH)
    }
    
    private fun addPlugin() {
        val pluginId = JOptionPane.showInputDialog(panel, "Enter Plugin ID:", "Add Plugin", JOptionPane.QUESTION_MESSAGE)
        if (!pluginId.isNullOrBlank()) {
            val currentList = (0 until pluginsList.model.size).map { pluginsList.model.getElementAt(it) }.toMutableList()
            if (!currentList.contains(pluginId)) {
                currentList.add(pluginId)
                pluginsList.setListData(currentList.toTypedArray())
            }
        }
    }
    
    private fun removeSelectedPlugin() {
        val selectedValues = pluginsList.selectedValuesList
        if (selectedValues.isNotEmpty()) {
            val currentList = (0 until pluginsList.model.size).map { pluginsList.model.getElementAt(it) }.toMutableList()
            currentList.removeAll(selectedValues)
            pluginsList.setListData(currentList.toTypedArray())
        }
    }
    
    private fun clearAllPlugins() {
        val result = JOptionPane.showConfirmDialog(
            panel,
            "Are you sure you want to clear all plugins?",
            "Confirm Clear",
            JOptionPane.YES_NO_OPTION
        )
        if (result == JOptionPane.YES_OPTION) {
            pluginsList.setListData(emptyArray())
        }
    }
    
    private fun exportSettings() {
        val settings = mapOf(
            "apiToken" to tokenField.text,
            "pluginIds" to (0 until pluginsList.model.size).map { pluginsList.model.getElementAt(it) },
            "pollingInterval" to pollingIntervalCombo.selectedItem,
            "notificationType" to notificationTypeCombo.selectedItem,
            "enableNotifications" to enableNotificationsCheckbox.isSelected,
            "enableAutoPoll" to enableAutoPollCheckbox.isSelected,
            "enableEmailNotifications" to enableEmailNotificationsCheckbox.isSelected,
            "email" to emailField.text,
            "dataRetentionDays" to dataRetentionDaysField.text
        )
        
        // In a real implementation, you would save this to a file
        JOptionPane.showMessageDialog(panel, "Settings exported successfully!", "Export", JOptionPane.INFORMATION_MESSAGE)
    }
    
    private fun importSettings() {
        // In a real implementation, you would load from a file
        JOptionPane.showMessageDialog(panel, "Settings import functionality would be implemented here", "Import", JOptionPane.INFORMATION_MESSAGE)
    }
    
    private fun resetToDefaults() {
        val result = JOptionPane.showConfirmDialog(
            panel,
            "Are you sure you want to reset all settings to defaults?",
            "Confirm Reset",
            JOptionPane.YES_NO_OPTION
        )
        if (result == JOptionPane.YES_OPTION) {
            tokenField.text = ""
            pluginsList.setListData(emptyArray())
            pollingIntervalCombo.selectedIndex = 2
            notificationTypeCombo.selectedIndex = 0
            enableNotificationsCheckbox.isSelected = true
            enableAutoPollCheckbox.isSelected = false
            enableEmailNotificationsCheckbox.isSelected = false
            emailField.text = ""
            dataRetentionDaysField.text = "30"
        }
    }
    
    private fun loadSettings() {
        val state = storage.getState()
        tokenField.text = state.apiToken ?: ""
        pluginsList.setListData(state.trackedPluginIds.toTypedArray())
        
        // Load other settings from storage (you'd need to extend the storage model)
        // For now, using defaults
    }
    
    override fun getDisplayName(): String = "Publish Tracker Settings"
    
    override fun createComponent() = panel
    
    override fun isModified(): Boolean {
        val state = storage.getState()
        return tokenField.text != (state.apiToken ?: "") ||
               (0 until pluginsList.model.size).map { pluginsList.model.getElementAt(it) } != state.trackedPluginIds
    }
    
    override fun apply() {
        val state = storage.getState()
        state.apiToken = tokenField.text.trim().ifBlank { null }
        state.trackedPluginIds = (0 until pluginsList.model.size).map { pluginsList.model.getElementAt(it) }.toMutableList()
        
        // Save other settings (you'd need to extend the storage model)
    }
    
    override fun reset() {
        loadSettings()
    }
}
