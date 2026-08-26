package util;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;

/**
 * Centralized UI styling for the StackIt app.
 *
 * Provides a consistent color palette, typography, and component
 * factory methods so every screen has the same modern look.
 *
 * Palette: "Indigo + Slate" 
 *   Primary  – indigo  #4F46E5
 *   Accent   – orange  #F97316
 *   Success  – green   #10B981
 *   Danger   – red     #EF4444
 *   Surface  – white   #FFFFFF on light gray #F7F8FA
 *   Text     – slate   #1E293B / #64748B
 */
public final class UIStyle {

    private UIStyle() { /* no instances */ }

    // Color palette
    public static final Color BG_PAGE       = new Color(0xF7, 0xF8, 0xFA);
    public static final Color BG_CARD       = Color.WHITE;
    public static final Color PRIMARY       = new Color(0x4F, 0x46, 0xE5);
    public static final Color PRIMARY_DARK  = new Color(0x43, 0x38, 0xCA);
    public static final Color ACCENT        = new Color(0xF9, 0x73, 0x16);
    public static final Color SUCCESS       = new Color(0x10, 0xB9, 0x81);
    public static final Color DANGER        = new Color(0xEF, 0x44, 0x44);
    public static final Color WARNING       = new Color(0xF5, 0x9E, 0x0B);
    public static final Color TEXT_DARK     = new Color(0x1E, 0x29, 0x3B);
    public static final Color TEXT_MUTED    = new Color(0x64, 0x74, 0x8B);
    public static final Color BORDER        = new Color(0xE2, 0xE8, 0xF0);
    public static final Color ROW_ALT       = new Color(0xF8, 0xFA, 0xFC);
    public static final Color HEADER_BG     = new Color(0x1E, 0x29, 0x3B);
    public static final Color TABLE_HEADER  = new Color(0xF1, 0xF5, 0xF9);

    // Typography 
    public static final String FONT_FAMILY = chooseFont(
        "SF Pro Text", "SF Pro Display", "Helvetica Neue",
        "Segoe UI", "Inter", "Avenir Next", "Roboto"
    );
    public static final String MONO_FAMILY = chooseFont(
        "SF Mono", "Menlo", "Consolas", "Courier New"
    );

    public static final Font H1     = new Font(FONT_FAMILY, Font.BOLD, 26);
    public static final Font H2     = new Font(FONT_FAMILY, Font.BOLD, 18);
    public static final Font H3     = new Font(FONT_FAMILY, Font.BOLD, 14);
    public static final Font BODY   = new Font(FONT_FAMILY, Font.PLAIN, 13);
    public static final Font BODY_B = new Font(FONT_FAMILY, Font.BOLD, 13);
    public static final Font BTN    = new Font(FONT_FAMILY, Font.BOLD, 13);
    public static final Font SMALL  = new Font(FONT_FAMILY, Font.PLAIN, 11);
    public static final Font TABLE  = new Font(FONT_FAMILY, Font.PLAIN, 13);
    public static final Font TABLE_HEAD = new Font(FONT_FAMILY, Font.BOLD, 12);

    private static String chooseFont(String... candidates) {
        String[] available = GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getAvailableFontFamilyNames();
        Set<String> set = new HashSet<>(Arrays.asList(available));
        for (String f : candidates) {
            if (set.contains(f)) return f;
        }
        return Font.SANS_SERIF;
    }

    // Button factories 
    public static JButton primaryButton(String text)  { return styled(text, PRIMARY, Color.WHITE); }
    public static JButton successButton(String text)  { return styled(text, SUCCESS, Color.WHITE); }
    public static JButton dangerButton(String text)   { return styled(text, DANGER,  Color.WHITE); }
    public static JButton accentButton(String text)   { return styled(text, ACCENT,  Color.WHITE); }

    /** Outlined / muted button – use for secondary actions like Cancel, Back. */
    public static JButton secondaryButton(String text) {
        JButton b = new JButton(text);
        b.setBackground(BG_CARD);
        b.setForeground(TEXT_DARK);
        b.setFont(BTN);
        b.setFocusPainted(false);
        b.setBorderPainted(true);
        b.setOpaque(true);
        b.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            BorderFactory.createEmptyBorder(7, 17, 7, 17)));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private static JButton styled(String text, Color bg, Color fg) {
        JButton b = new JButton(text);
        b.setBackground(bg);
        b.setForeground(fg);
        b.setFont(BTN);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setOpaque(true);
        b.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    // Label / field helpers 
    public static JLabel h1(String text) {
        JLabel l = new JLabel(text);
        l.setFont(H1);
        l.setForeground(TEXT_DARK);
        return l;
    }

    public static JLabel h2(String text) {
        JLabel l = new JLabel(text);
        l.setFont(H2);
        l.setForeground(TEXT_DARK);
        return l;
    }

    public static JLabel formLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(BODY_B);
        l.setForeground(TEXT_DARK);
        return l;
    }

    public static JLabel mutedLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(SMALL);
        l.setForeground(TEXT_MUTED);
        return l;
    }

    public static JLabel centeredLabel(String text, Font font, Color color) {
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setFont(font);
        l.setForeground(color);
        return l;
    }

    /** Soft border for input fields. */
    public static Border fieldBorder() {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            BorderFactory.createEmptyBorder(6, 10, 6, 10));
    }
}
