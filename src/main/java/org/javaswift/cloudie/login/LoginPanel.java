/*
 *  Copyright 2012-2013 E.Hooijmeijer
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.javaswift.cloudie.login;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.javaswift.cloudie.CloudiePanel;
import org.javaswift.cloudie.login.CredentialsStore.Credentials;
import org.javaswift.cloudie.util.LabelComponentPanel;
import org.javaswift.cloudie.util.ReflectionAction;


public class LoginPanel extends JPanel {

    public interface LoginCallback {
        void doLogin(String authUrl, String tenant, String username, char[] pass);
    }

    private Action okAction = new ReflectionAction<LoginPanel>("Ok", CloudiePanel.getIcon("server_connect.png"), this, "onOk");
    private Action saveAction = new ReflectionAction<LoginPanel>("", CloudiePanel.getIcon("table_save.png"), this, "onSave");
    private Action deleteAction = new ReflectionAction<LoginPanel>("", CloudiePanel.getIcon("table_delete.png"), this, "onDelete");
    private Action cancelAction = new ReflectionAction<LoginPanel>("Cancel", this, "onCancel");

    private JButton okButton = new JButton(okAction);
    private JButton cancelButton = new JButton(cancelAction);
    private JButton saveButton = new JButton(saveAction);
    private JButton deleteButton = new JButton(deleteAction);

    private DefaultComboBoxModel model = new DefaultComboBoxModel();
    private JComboBox savedCredentials = new JComboBox(model);
    private JTextField authUrl = new JTextField();
    private JTextField tenant = new JTextField();
    private JTextField username = new JTextField();
    private JPasswordField password = new JPasswordField();
    private JLabel warningLabel = new JLabel("Saved Credentials are stored in Plain-Text.", CloudiePanel.getIcon("table_error.png"), JLabel.CENTER);

    private LoginCallback callback;
    private JDialog owner;
    private ActionListener comboActionListener;
    private CredentialsStore credentialsStore;

    public LoginPanel(LoginCallback callback, CredentialsStore credentialsStore) {
        super(new BorderLayout(0, 0));
        this.callback = callback;
        this.credentialsStore = credentialsStore;
        setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        Box outer = Box.createVerticalBox();
        Box box = Box.createVerticalBox();
        Box btn = Box.createHorizontalBox();
        box.setBorder(BorderFactory.createTitledBorder("Credentials"));
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JPanel warn = new JPanel(new BorderLayout());
        warn.setBorder(BorderFactory.createEmptyBorder(8, 0, 4, 0));
        warn.add(warningLabel, BorderLayout.CENTER);
        buttons.setBorder(BorderFactory.createEtchedBorder());
        buttons.add(okButton);
        buttons.add(cancelButton);
        //
        saveButton.setToolTipText("Save Credentials");
        deleteButton.setToolTipText("Delete Credentials");
        btn.add(saveButton);
        btn.add(deleteButton);
        //
        box.add(new LabelComponentPanel("", savedCredentials, btn));
        box.add(new LabelComponentPanel("AuthURL", authUrl));
        box.add(new LabelComponentPanel("Tenant", tenant));
        box.add(new LabelComponentPanel("Username", username));
        box.add(new LabelComponentPanel("Password", password));
        //
        outer.add(box);
        outer.add(warn);
        this.add(outer, BorderLayout.NORTH);
        this.add(buttons, BorderLayout.SOUTH);
        //
        bindSelectionListener();
        refreshCredentials();
        enableDisable();
        bindDocumentListeners();
    }

    private void bindSelectionListener() {
        comboActionListener = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedIndex = savedCredentials.getSelectedIndex();
                if (selectedIndex <= 0) {
                    clearLoginForm();
                } else {
                    Credentials cr = (Credentials) savedCredentials.getSelectedItem();
                    if (cr != null) {
                        authUrl.setText(cr.authUrl);
                        tenant.setText(cr.tenant);
                        username.setText(cr.username);
                        password.setText(String.valueOf(cr.password));
                        enableDisable();
                    }
                }
            }
        };
        savedCredentials.addActionListener(comboActionListener);
    }

    private void clearLoginForm() {
        authUrl.setText("");
        tenant.setText("");
        username.setText("");
        password.setText("");
        enableDisable();
    }

    private void refreshCredentials() {
        savedCredentials.removeActionListener(comboActionListener);
        try {
            model.removeAllElements();
            for (Credentials cr : credentialsStore.getAvailableCredentials()) {
                model.addElement(cr);
            }
            if (model.getSize() > 0) {
                Credentials credentials = new Credentials();
                credentials.tenant = "";
                credentials.username = "";
                credentials.password = new char[0];
                credentials.authUrl = "";
                model.insertElementAt(credentials, 0);
                savedCredentials.setSelectedIndex(0);
            }
        } finally {
            savedCredentials.addActionListener(comboActionListener);
        }
    }

    private void bindDocumentListeners() {
        DocumentListener lst = new DocumentListener() {

            @Override
            public void removeUpdate(DocumentEvent e) {
                enableDisable();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                enableDisable();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                enableDisable();
            }
        };
        authUrl.getDocument().addDocumentListener(lst);
        tenant.getDocument().addDocumentListener(lst);
        username.getDocument().addDocumentListener(lst);
        password.getDocument().addDocumentListener(lst);
    }

    private void enableDisable() {
        saveAction.setEnabled(isAuthComplete());
        deleteAction.setEnabled(savedCredentials.getSelectedIndex() > 0);
        savedCredentials.setEnabled(model.getSize() > 0);
    }

    private boolean isAuthComplete() {
        return !authUrl.getText().isEmpty() && !tenant.getText().isEmpty() && !username.getText().isEmpty() && !(password.getPassword().length == 0);
    }

    public void onShow() {
        authUrl.requestFocus();
    }

    public void onOk() {
        callback.doLogin(authUrl.getText().trim(), tenant.getText().trim(), username.getText().trim(), password.getPassword());
    }

    public void onCancel() {
        owner.setVisible(false);
    }

    public void onSave() {
        Credentials cr = new Credentials();
        cr.authUrl = authUrl.getText().trim();
        cr.tenant = tenant.getText().trim();
        cr.username = username.getText().trim();
        cr.password = password.getPassword();
        credentialsStore.save(cr);
        refreshCredentials();
        savedCredentials.setSelectedItem(cr);
        enableDisable();
    }

    public void onDelete() {
        if (confirm("Are you sure you want to remove these credentials?")) {
            credentialsStore.delete((Credentials) savedCredentials.getSelectedItem());
            refreshCredentials();
            clearLoginForm();
            enableDisable();
        }
    }

    public void setOwner(JDialog dialog) {
        this.owner = dialog;
        this.owner.getRootPane().setDefaultButton(okButton);
    }

    public boolean confirm(String message) {
        return JOptionPane.showConfirmDialog(this, message, "Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION;
    }

}
