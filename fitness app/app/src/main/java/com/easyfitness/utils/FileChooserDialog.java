package com.easyfitness.utils;

import android.R.layout;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;
import android.text.Editable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.easyfitness.R;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FileChooserDialog {
    private boolean m_isNewFolderEnabled = false;
    private boolean m_displayFolderOnly = false;
    private String m_fileFilter = "*";
    private String m_sdcardDirectory = "";
    private Context m_context;
    private TextView m_titleView;

    private String m_dir = "";
    private List<String> m_subdirs = null;
    private ChosenFileListener m_chosenFileListener = null;
    private ArrayAdapter<String> m_listAdapter = null;

    public FileChooserDialog(Context context, ChosenFileListener chosenDirectoryListener) {
        m_context = context;
        m_sdcardDirectory = Environment.getExternalStorageDirectory().getAbsolutePath();
        m_chosenFileListener = chosenDirectoryListener;

        try {
            m_sdcardDirectory = new File(m_sdcardDirectory).getCanonicalPath();
        } catch (IOException ignored) {
        }
    }

    public boolean getNewFolderEnabled() {
        return m_isNewFolderEnabled;
    }
    public void setNewFolderEnabled(boolean isNewFolderEnabled) {
        m_isNewFolderEnabled = isNewFolderEnabled;
    }

    public boolean getDisplayFolderOnly() {
        return m_displayFolderOnly;
    }
    public void setDisplayFolderOnly(boolean displayFolderOnly) {
        m_displayFolderOnly = displayFolderOnly;
    }

    public String getFileFilter() {
        return m_fileFilter;
    }
    public void setFileFilter(String fileFilter) {
        m_fileFilter = fileFilter;
    }

    public void resetFileFilter(String fileFilter) {
        m_fileFilter = fileFilter;
    }

    public void chooseDirectory() {
        chooseDirectory(m_sdcardDirectory);
    }

    public void chooseDirectory(String dir) {
        File dirFile = new File(dir);
        if (!dirFile.exists() || !dirFile.isDirectory()) {
            dir = m_sdcardDirectory;
        }

        try {
            dir = new File(dir).getCanonicalPath();
        } catch (IOException ioe) {
            return;
        }

        m_dir = dir;
        m_subdirs = getDirectories(dir);

        class DirectoryOnClickListener implements DialogInterface.OnClickListener {
            public void onClick(DialogInterface dialog, int item) {

                if (((AlertDialog) dialog).getListView().getAdapter().getItem(item).toString().substring(0, 1).equals("/")) {
                    m_dir += ((AlertDialog) dialog).getListView().getAdapter().getItem(item);
                    ((AlertDialog) dialog).getListView().smoothScrollToPositionFromTop(0, 0, 0);// Back on top of the ListView
                    updateDirectory();
                } else if (((AlertDialog) dialog).getListView().getAdapter().getItem(item).toString().equals("..")) {
                    m_dir = new File(m_dir).getParent();
                    ((AlertDialog) dialog).getListView().smoothScrollToPositionFromTop(0, 0, 0);//.scrollTo(0, 0);//.smoothScrollToPosition(0);
                    updateDirectory();
                } else {
                    if (m_chosenFileListener != null) {
                        m_chosenFileListener.onChosenFile(m_dir + "/" + ((AlertDialog) dialog).getListView().getAdapter().getItem(item).toString());
                        dialog.dismiss();
                    }
                }
            }
        }

        AlertDialog.Builder dialogBuilder =
            createDirectoryChooserDialog(dir, m_subdirs, new DirectoryOnClickListener());

        dialogBuilder.setNegativeButton("Cancel", null);

        final AlertDialog dirsDialog = dialogBuilder.create();
        dirsDialog.show();
    }
    private boolean createSubDir(String newDir) {
        File newDirFile = new File(newDir);
        if (!newDirFile.exists()) {
            return newDirFile.mkdir();
        }

        return false;
    }

    public List<String> getDirectories(String dir) {
        List<String> dirs = new ArrayList<>();

        try {
            File dirFile = new File(dir);
            if (!dirFile.exists() || !dirFile.isDirectory()) {
                return dirs;
            }

            if (dir.length() > 1) dirs.add("..");

            for (File file : dirFile.listFiles()) {
                if (file.isDirectory())
                {
                    dirs.add("/" + file.getName());
                } else
                {
                    if (!this.m_displayFolderOnly)
                        if (isInFilter(file.getName())) {
                            dirs.add(file.getName());
                        }
                }
            }
        } catch (Exception ignored) {
        }

        Collections.sort(dirs, String::compareTo);

        return dirs;
    }
    public List<String> getFiles(String dir) {
        List<String> dirs = new ArrayList<>();

        try {
            File dirFile = new File(dir);
            if (!dirFile.exists() || !dirFile.isDirectory()) {
                return dirs;
            }

            for (File file : dirFile.listFiles()) {
                if (!file.isDirectory())
                {
                    if (!this.m_displayFolderOnly)
                        if (isInFilter(file.getName())) {
                            dirs.add(file.getName());
                        }
                }
            }
        } catch (Exception ignored) {
        }

        Collections.sort(dirs, String::compareTo);

        return dirs;
    }
    private boolean isInFilter(String fileName) {
        boolean ret = false;
        String extension = "";
        extension = getExtension(fileName);
        if (this.m_fileFilter.contains("*"))
            return true;
        if (this.m_fileFilter.contains(extension))
            return true;

        return ret;
    }

    private String getExtension(String fileName) {
        String extension = "";

        int i = fileName.lastIndexOf('.');
        int p = Math.max(fileName.lastIndexOf('/'), fileName.lastIndexOf('\\'));

        if (i > p) {
            extension = fileName.substring(i + 1);
        }
        return extension;
    }

    private AlertDialog.Builder createDirectoryChooserDialog(String title, List<String> listItems,
                                                             DialogInterface.OnClickListener onClickListener) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(m_context);
        LinearLayout titleLayout = new LinearLayout(m_context);
        titleLayout.setOrientation(LinearLayout.VERTICAL);

        m_titleView = new TextView(m_context);
        m_titleView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        m_titleView.setTextAppearance(m_context, android.R.style.TextAppearance_DeviceDefault_Medium);
        m_titleView.setTextColor(m_context.getResources().getColor(android.R.color.black));
        m_titleView.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
        m_titleView.setText(title);

        Button newDirButton = new Button(m_context);
        newDirButton.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        newDirButton.setText("New folder");
        newDirButton.setOnClickListener(v -> {
            final EditText input = new EditText(m_context);
            new AlertDialog.Builder(m_context).
                setTitle("New folder name").
                setView(input).setPositiveButton(m_context.getResources().getText(R.string.global_ok), (dialog, whichButton) -> {
                Editable newDir = input.getText();
                String newDirName = newDir.toString();
                if (createSubDir(m_dir + "/" + newDirName)) {
                    m_dir += "/" + newDirName;
                    updateDirectory();
                } else {
                    Toast.makeText(
                        m_context, m_context.getResources().getText(R.string.failedtocreatefolder) + " " + newDirName, Toast.LENGTH_SHORT).show();
                }
            }).setNegativeButton(m_context.getResources().getText(R.string.global_cancel), null).show();
        });

        if (!m_isNewFolderEnabled) {
            newDirButton.setVisibility(View.GONE);
        }

        titleLayout.addView(m_titleView);
        titleLayout.addView(newDirButton);

        dialogBuilder.setCustomTitle(titleLayout);

        m_listAdapter = createListAdapter(listItems);

        dialogBuilder.setSingleChoiceItems(m_listAdapter, -1, onClickListener);
        dialogBuilder.setCancelable(false);

        return dialogBuilder;
    }

    private void updateDirectory() {
        m_subdirs.clear();
        m_subdirs.addAll(getDirectories(m_dir));
        m_titleView.setText(m_dir);
        m_listAdapter.notifyDataSetChanged();

    }

    private ArrayAdapter<String> createListAdapter(List<String> items) {
        return new ArrayAdapter<String>(m_context,
            layout.simple_list_item_1, android.R.id.text1, items)
        {
            @Override
            public View getView(int position, View convertView,
                                ViewGroup parent) {
                View v = super.getView(position, convertView, parent);

                if (v instanceof TextView) {
                    TextView tv = (TextView) v;
                    tv.getLayoutParams().height = LayoutParams.WRAP_CONTENT;
                    tv.setEllipsize(null);
                    tv.setTextAppearance(m_context, android.R.style.TextAppearance_DeviceDefault_Small);
                }
                return v;
            }
        };
    }
    public interface ChosenFileListener {
        void onChosenFile(String chosenDir);
    }
}

