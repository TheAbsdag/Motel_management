package model.email.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MarkdownConverterTest {

    private final MarkdownConverter converter = new MarkdownConverter();

    @Test
    void convertsBold() {
        assertEquals("<p>texto <strong>negrita</strong> mas</p>", converter.toHtml("texto **negrita** mas"));
    }

    @Test
    void convertsItalic() {
        assertEquals("<p>texto <em>cursiva</em> mas</p>", converter.toHtml("texto *cursiva* mas"));
    }

    @Test
    void convertsUnderline() {
        assertEquals("<p>texto <u>subrayado</u> mas</p>", converter.toHtml("texto __subrayado__ mas"));
    }

    @Test
    void convertsHeading1() {
        assertEquals("<h1>Titulo</h1>", converter.toHtml("# Titulo").strip());
    }

    @Test
    void convertsHeading2() {
        assertEquals("<h2>Subtitulo</h2>", converter.toHtml("## Subtitulo").strip());
    }

    @Test
    void convertsHeading3() {
        assertEquals("<h3>Seccion</h3>", converter.toHtml("### Seccion").strip());
    }

    @Test
    void convertsUnorderedList() {
        String result = converter.toHtml("- item1\n- item2").strip();
        assertTrue(result.contains("<ul>"));
        assertTrue(result.contains("<li>item1</li>"));
        assertTrue(result.contains("<li>item2</li>"));
        assertTrue(result.contains("</ul>"));
    }

    @Test
    void convertsOrderedList() {
        String result = converter.toHtml("1. item1\n2. item2").strip();
        assertTrue(result.contains("<ol>"));
        assertTrue(result.contains("<li>item1</li>"));
        assertTrue(result.contains("<li>item2</li>"));
        assertTrue(result.contains("</ol>"));
    }

    @Test
    void convertsLink() {
        assertEquals("<p>visita <a href=\"https://ejemplo.com\">Ejemplo</a> ahora</p>",
                converter.toHtml("visita [Ejemplo](https://ejemplo.com) ahora"));
    }

    @Test
    void convertsHr() {
        assertEquals("<hr>", converter.toHtml("---").strip());
    }

    @Test
    void convertsParagraphs() {
        String result = converter.toHtml("linea1\n\nlinea2");
        assertTrue(result.contains("<p>"));
        assertTrue(result.contains("</p>"));
    }

    @Test
    void convertsNewlines() {
        String result = converter.toHtml("linea1\nlinea2");
        assertTrue(result.contains("<br>"));
    }

    @Test
    void handlesNull() {
        assertNull(converter.toHtml(null));
    }

    @Test
    void handlesEmpty() {
        assertEquals("", converter.toHtml(""));
    }

    @Test
    void convertsInlineWithinHeading() {
        assertEquals("<h1>Titulo con <strong>negrita</strong></h1>",
                converter.toHtml("# Titulo con **negrita**").strip());
    }

    @Test
    void convertsMixedInline() {
        assertEquals("<p>texto <strong>bold</strong> y <em>italic</em> y <u>underline</u></p>",
                converter.toHtml("texto **bold** y *italic* y __underline__"));
    }
}
