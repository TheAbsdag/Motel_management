package model.email.service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class MarkdownConverter {

    private static final Pattern BOLD     = Pattern.compile("\\*\\*(.+?)\\*\\*");
    private static final Pattern ITALIC   = Pattern.compile("(?<!\\*)\\*(?!\\*)(.+?)(?<!\\*)\\*(?!\\*)");
    private static final Pattern UNDERLINE = Pattern.compile("__(.+?)__");
    private static final Pattern LINK     = Pattern.compile("\\[([^\\]]+)\\]\\(([^)]+)\\)");

    public String toHtml(String markdown) {
        if (markdown == null) return null;
        if (markdown.isBlank()) return "";

        String[] lines = markdown.split("\n", -1);
        StringBuilder html = new StringBuilder();
        boolean inParagraph = false;
        List<String> ulBuffer = new ArrayList<>();
        List<String> olBuffer = new ArrayList<>();

        for (String raw : lines) {
            String trimmed = raw.strip();

            if (trimmed.startsWith("### ")) {
                flushParagraph(html, inParagraph);
                inParagraph = false;
                flushList(html, ulBuffer, "ul");
                flushList(html, olBuffer, "ol");
                html.append("<h3>").append(renderInline(trimmed.substring(4).strip())).append("</h3>\n");
            } else if (trimmed.startsWith("## ")) {
                flushParagraph(html, inParagraph);
                inParagraph = false;
                flushList(html, ulBuffer, "ul");
                flushList(html, olBuffer, "ol");
                html.append("<h2>").append(renderInline(trimmed.substring(3).strip())).append("</h2>\n");
            } else if (trimmed.startsWith("# ")) {
                flushParagraph(html, inParagraph);
                inParagraph = false;
                flushList(html, ulBuffer, "ul");
                flushList(html, olBuffer, "ol");
                html.append("<h1>").append(renderInline(trimmed.substring(2).strip())).append("</h1>\n");
            } else if (trimmed.equals("---")) {
                flushParagraph(html, inParagraph);
                inParagraph = false;
                flushList(html, ulBuffer, "ul");
                flushList(html, olBuffer, "ol");
                html.append("<hr>\n");
            } else if (trimmed.startsWith("- ")) {
                inParagraph = false;
                flushList(html, olBuffer, "ol");
                ulBuffer.add(renderInline(trimmed.substring(2).strip()));
            } else if (trimmed.matches("^\\d+\\.\\s.*")) {
                inParagraph = false;
                flushList(html, ulBuffer, "ul");
                olBuffer.add(renderInline(trimmed.replaceFirst("^\\d+\\.\\s", "")));
            } else if (trimmed.isEmpty()) {
                if (inParagraph) {
                    html.append("</p>\n");
                    inParagraph = false;
                }
                flushList(html, ulBuffer, "ul");
                flushList(html, olBuffer, "ol");
            } else {
                flushList(html, ulBuffer, "ul");
                flushList(html, olBuffer, "ol");
                if (!inParagraph) {
                    html.append("<p>");
                    inParagraph = true;
                } else {
                    html.append("<br>\n");
                }
                html.append(renderInline(trimmed));
            }
        }

        if (inParagraph) html.append("</p>\n");
        flushList(html, ulBuffer, "ul");
        flushList(html, olBuffer, "ol");

        return html.toString().strip();
    }

    private String renderInline(String text) {
        if (text == null) return "";
        text = LINK.matcher(text).replaceAll(m -> "<a href=\"" + m.group(2) + "\">" + m.group(1) + "</a>");
        text = BOLD.matcher(text).replaceAll("<strong>$1</strong>");
        text = UNDERLINE.matcher(text).replaceAll("<u>$1</u>");
        text = ITALIC.matcher(text).replaceAll("<em>$1</em>");
        return text;
    }

    private void flushParagraph(StringBuilder html, boolean inParagraph) {
        if (inParagraph) html.append("</p>\n");
    }

    private void flushList(StringBuilder html, List<String> buffer, String tag) {
        if (buffer.isEmpty()) return;
        html.append("<").append(tag).append(">\n");
        for (String item : buffer) {
            html.append("  <li>").append(item).append("</li>\n");
        }
        html.append("</").append(tag).append(">\n");
        buffer.clear();
    }
}
