package ru.nsu.romchela.db.view;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.metal.MetalToggleButtonUI;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Method;
import java.security.InvalidParameterException;
import java.util.logging.Logger;

public class MainFrame extends JFrame {
    private static final long serialVersionUID = 1L;
    private JMenuBar menuBar;
    protected JToolBar toolBar;

    private static final String menuPathError = "Menu path error: ";

    public MainFrame() {
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        menuBar = new JMenuBar();
        setJMenuBar(menuBar);
        toolBar = new JToolBar("Main toolbar");
        toolBar.setRollover(true);
        add(toolBar, BorderLayout.PAGE_START);
    }

    public MainFrame(int x, int y, String title) {
        this();
        setSize(x, y);
        setLocationByPlatform(true);
        setTitle(title);
    }


    public JMenuItem createMenuItem(String title, String tooltip, int mnemonic, String icon, String actionMethod)
            throws SecurityException, NoSuchMethodException {
        JMenuItem item = new JMenuItem(title);
        item.setMnemonic(mnemonic);
        item.setToolTipText(tooltip);
        if (icon != null) {
            item.setIcon(new ImageIcon(getClass().getResource("/" + icon), title));
        }
        final Method method = getClass().getMethod(actionMethod);
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                try {
                    method.invoke(MainFrame.this);
                } catch (Exception e) {
                    Logger.getGlobal().warning("invoke of action method was failed");
                }
            }
        });
        return item;
    }


    public JMenuItem createMenuItem(String title, String tooltip, int mnemonic, String actionMethod) throws SecurityException, NoSuchMethodException {
        return createMenuItem(title, tooltip, mnemonic, null, actionMethod);
    }

    public JMenu createSubMenu(String title, int mnemonic) {
        JMenu menu = new JMenu(title);
        menu.setMnemonic(mnemonic);
        return menu;
    }

    @SuppressWarnings("Duplicates")
    public void addSubMenu(String title, int mnemonic) {
        MenuElement element = getParentMenuElement(title);
        if (element == null) {
            throw new InvalidParameterException(menuPathError + title);
        }
        JMenu subMenu = createSubMenu(getMenuPathName(title), mnemonic);
        if (element instanceof JMenuBar) {
            ((JMenuBar) element).add(subMenu);
        } else if (element instanceof JMenu) {
            ((JMenu) element).add(subMenu);
        } else if (element instanceof JPopupMenu) {
            ((JPopupMenu) element).add(subMenu);
        } else {
            throw new InvalidParameterException("Invalid menu path: " + title);
        }
    }

    @SuppressWarnings("Duplicates")
    public void addMenuItem(String title, String tooltip, int mnemonic, String icon, String actionMethod) throws SecurityException, NoSuchMethodException {
        MenuElement element = getParentMenuElement(title);
        if (element == null) {
            throw new InvalidParameterException(menuPathError + title);
        }
        JMenuItem item = createMenuItem(getMenuPathName(title), tooltip, mnemonic, icon, actionMethod);
        if (element instanceof JMenu) {
            ((JMenu) element).add(item);
        } else if (element instanceof JPopupMenu) {
            ((JPopupMenu) element).add(item);
        } else {
            throw new InvalidParameterException("Invalid menu path: " + title);
        }
    }

    public void addMenuItem(String title, String tooltip, int mnemonic, String actionMethod) throws SecurityException, NoSuchMethodException {
        addMenuItem(title, tooltip, mnemonic, null, actionMethod);
    }

    public void addMenuSeparator(String title) {
        MenuElement element = getMenuElement(title);
        if (element == null) {
            throw new InvalidParameterException(menuPathError + title);
        }
        if (element instanceof JMenu) {
            ((JMenu) element).addSeparator();
        }
        else if (element instanceof JPopupMenu) {
            ((JPopupMenu) element).addSeparator();
        } else {
            throw new InvalidParameterException("Invalid menu path: " + title);
        }
    }

    private String getMenuPathName(String menuPath) {
        int pos = menuPath.lastIndexOf('/');
        if (pos > 0) {
            return menuPath.substring(pos + 1);
        } else {
            return menuPath;
        }
    }

    private MenuElement getParentMenuElement(String menuPath) {
        int pos = menuPath.lastIndexOf('/');
        if (pos > 0) {
            return getMenuElement(menuPath.substring(0, pos));
        } else {
            return menuBar;
        }
    }

    public JToggleButton getButtonFromToolbar(String menuPath) {
        for (int i = 0; i < toolBar.getComponentCount(); i++) {
            Component component = toolBar.getComponentAtIndex(i);
            if (component instanceof JToggleButton) {
                JToggleButton button = (JToggleButton) component;
                if (button.getName().equals(menuPath)) {
                    return button;
                }
            }
        }
        return null;
    }

    public MenuElement getMenuElement(String menuPath) {
        MenuElement element = menuBar;
        for (String pathElement : menuPath.split("/")) {
            MenuElement newElement = null;
            for (MenuElement subElement : element.getSubElements()) {
                if (subElement instanceof JMenu && ((JMenu) subElement).getText().equals(pathElement)
                        || subElement instanceof JMenuItem && ((JMenuItem) subElement).getText().equals(pathElement)) {
                    if (subElement.getSubElements().length == 1 && subElement.getSubElements()[0] instanceof JPopupMenu) {
                        newElement = subElement.getSubElements()[0];
                    } else {
                        newElement = subElement;
                    }
                    break;
                }
            }

            if (newElement == null) {
                return null;
            }
            element = newElement;
        }
        return element;
    }

    public JButton createToolBarButton(JMenuItem item) {
        JButton button = new JButton(item.getIcon());
        for (ActionListener listener : item.getActionListeners()) {
            button.addActionListener(listener);
        }
        button.setToolTipText(item.getToolTipText());
        button.setBorderPainted(true);
        Border line = new LineBorder(Color.LIGHT_GRAY);
        Border margin = new EmptyBorder(2, 5, 2, 5);
        Border compound = new CompoundBorder(line, margin);
        button.setBorder(compound);
        button.setVisible(true);
        return button;
    }

    public JToggleButton createToolBarToggleButton(String menuPath, JMenuItem item, boolean selected) {
        JToggleButton button = new JToggleButton(item.getIcon(), selected);
        button.setName(menuPath);
        for (ActionListener listener : item.getActionListeners()) {
            button.addActionListener(listener);
        }
        button.setToolTipText(item.getToolTipText());
        button.setBorderPainted(true);
        Border line = new LineBorder(Color.LIGHT_GRAY);
        Border margin = new EmptyBorder(2, 5, 2, 5);
        Border compound = new CompoundBorder(line, margin);
        button.setBorder(compound);
        button.setUI(new MetalToggleButtonUI() {
            @Override
            protected Color getSelectColor() {
                return new Color(200, 200, 200);
            }
        });
        return button;
    }

    public JButton createToolBarButton(String menuPath) {
        JMenuItem item = (JMenuItem) getMenuElement(menuPath);
        if (item == null) {
            throw new InvalidParameterException(menuPathError + menuPath);
        }
        return createToolBarButton(item);
    }

    public JToggleButton createToolBarToggleButton(String menuPath, boolean selected) {
        JMenuItem item = (JMenuItem) getMenuElement(menuPath);
        if (item == null) {
            throw new InvalidParameterException(menuPathError + menuPath);
        }
        return createToolBarToggleButton(menuPath, item, selected);
    }

    public JButton addToolBarButton(String menuPath) {
        JButton button = createToolBarButton(menuPath);
        toolBar.add(button);
        return button;
    }

    public void addToolBarToggleButton(String menuPath, boolean selected) {
        toolBar.add(createToolBarToggleButton(menuPath, selected));
    }

    public void addToolBarSeparator() {
        toolBar.addSeparator();
    }

}