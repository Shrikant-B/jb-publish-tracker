package com.shrikantbadwaik.publish.time.tracker.settings

import com.intellij.openapi.options.Configurable
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextField
import com.shrikantbadwaik.publish.time.tracker.data.PublishTrackerStorage
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JPanel

class PublishSettingsConfigurable : Configurable {
    private val panel = JPanel()
    private val tokenField = JBTextField()
    private val pluginsList = JBList<String>()
    private val storage = PublishTrackerStorage.getInstance()

    init {
        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)
        panel.add(JLabel("JetBrains Marketplace API Token"))
        panel.add(tokenField)
        panel.add(Box.createVerticalStrut(8))
        panel.add(JLabel("Plugin IDs to track (one per line)"))
        val scroll = JBScrollPane(pluginsList)
        panel.add(scroll)
        val addBtn = JButton("Add Plugin ID")
        addBtn.addActionListener {
            val id = tokenField.text.trim()
            if (id.isNotBlank()) {
                val current = storage.getState().trackedPluginIds
                if (!current.contains(id)) {
                    current.add(id)
                    pluginsList.setListData(current.toTypedArray())
                }
            }
        }
        panel.add(addBtn)
    }

    override fun getDisplayName(): String = "JBPublish Tracker"

    override fun createComponent() = panel

    override fun isModified(): Boolean {
        // Minimal: token and plugin list managed through storage directly
        return false
    }

    override fun apply() {
        storage.getState().apiToken = tokenField.text.trim().ifBlank { null }
        storage.getState().trackedPluginIds = pluginsList.selectedValuesList.toMutableList()
    }

    override fun reset() {
        tokenField.text = storage.getState().apiToken ?: ""
        pluginsList.setListData(storage.getState().trackedPluginIds.toTypedArray())
    }
}
