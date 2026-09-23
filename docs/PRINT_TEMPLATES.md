---
title: Print Templates
description: Layout format, available fields and how to customize the printed receipts and turn reports
author: TheAbsdag
---

# Print Templates

Since **v0.1.5** the four documents the program prints are laid out from JSON templates instead of being fixed in code:

| Type | Document | Printed when |
|------|----------|--------------|
| `ROOM_RECEIPT` | Recibo de habitación | A room booking is confirmed |
| `SALE_RECEIPT` | Recibo de venta | A sale is checked out |
| `TURN_SUMMARY` | Resumen de turno | The turn is closed |
| `TURN_DETAIL` | Detalle de turno | The turn report is printed |

A type resolves in this order when printing:

1. `data/printTemplates/<type>.json` — the customized template, written by the editor.
2. The built-in layout in `Printer` — used when no customized template exists.

So a fresh installation prints correctly with nothing configured: templates are an addition, never a requirement.

---

## Where templates live

| | Path |
|---|---|
| Customized | `data/printTemplates/<type>.json` (next to the running `.jar`; created on first save) |
| Factory defaults | `/printTemplates/<type>.json` inside the `.jar`, read as a classpath resource |

`<type>` is the enum name in lower case: `room_receipt.json`, `sale_receipt.json`, `turn_summary.json`, `turn_detail.json`.

The **factory defaults are what the editor loads** when a type has not been customized yet. They describe the same layout as the built-in one, so saving a default without changing anything is a no-op in practice.

---

## Editing from the program

*Options → Printer configuration → **CONFIGURAR IMPRESION*** opens the template editor, which holds every action:

| Control | Effect |
|---------|--------|
| Template selector (`PLANTILLA:`) | Switches between the four documents |
| `AÑADIR` | Adds a line to the selected band |
| `SUBIR` / `BAJAR` | Moves the selected line inside its band |
| `DUPLICAR` | Copies the selected line |
| `ELIMINAR` | Removes the selected line |
| `+ SEGMENTO` / `- SEGMENTO` | Adds or removes a segment in the selected line |
| `BANDA:` | Moves the selected line to `HEADER`, `BODY` or `FOOTER` |
| `CENTRADA` | Centers the line on the paper |
| `ESPACIADORES:` | Number of blank rows printed after the line |
| `PAPEL PREVIA:` | Preview paper: `Declarado` (what the configured printer declares), `58 mm`, `80 mm` or `Carta` |
| `PAPEL -` / zoom icons | Preview zoom, 50 % to 300 % in 25 % steps |
| `IMPRIMIR PRUEBA` | Prints the **unsaved** template with sample data, through the same print job receipts use |
| `RESTAURAR POR DEFECTO` | Loads the factory default into the editor (still needs `GUARDAR`) |
| `BORRAR PLANTILLA` | Deletes the customized file; the built-in layout is used again |
| `GUARDAR` | Writes `data/printTemplates/<type>.json`; it is used from the next print on |
| `VOLVER` | Leaves the editor, asking first when there are unsaved changes |

The preview draws each line on a sheet as wide as the selected paper, in real screen pixels, so a line that wraps on paper wraps in the preview. The page counter turns red once the document no longer fits in one page.

`IMPRIMIR PRUEBA` writes no PDF and touches neither the turn, the cash register nor the printer configuration: it only sends the unsaved copy to the printer.

---

## File format

A template is three nested levels: **bands → lines → segments**.

```json
{
  "templateType": "ROOM_RECEIPT",
  "bands": [
    {
      "name": "HEADER",
      "lines": [
        {
          "segments": [
            { "type": "FIELD", "text": "motelName", "style": "LargeStyle", "bold": false }
          ],
          "centered": false,
          "spacerAfter": 0
        },
        {
          "segments": [
            { "type": "TEXT", "text": "  FACTURA No. ", "style": "DefaultStyle", "bold": false },
            { "type": "FIELD", "text": "consecutive", "style": "DefaultStyle", "bold": true }
          ],
          "centered": false,
          "spacerAfter": 1
        }
      ]
    }
  ]
}
```

| Field | Meaning |
|-------|---------|
| `templateType` | Must match the type of the file (`ROOM_RECEIPT`, `SALE_RECEIPT`, `TURN_SUMMARY`, `TURN_DETAIL`) |
| `bands[].name` | Band label; the editor uses `HEADER`, `BODY` and `FOOTER`, and prints the bands in the order they appear |
| `lines[].segments` | What the line prints, left to right |
| `lines[].centered` | `true` centers the whole line |
| `lines[].spacerAfter` | Number of blank rows printed after the line |
| `segments[].type` | `TEXT` for literal text, `FIELD` for a value of the document |
| `segments[].text` | The literal text, or the field key when `type` is `FIELD` |
| `segments[].style` | Style name from the table below; unknown names fall back to `DefaultStyle` |
| `segments[].bold` | `true` uses the bold variant of the style |

Notes on rendering, all of them applying to the editor preview and the printed output alike:

- **Fields are resolved with the data of the document being printed.** A key the document does not provide prints as empty text.
- **A line whose segments all resolve to empty text is not printed at all** — not even as a blank row. This is how optional lines (`extensionDuration`, `legalText`, a refund total) disappear on their own when there is nothing to show.
- `\n` inside `text` starts a new line, `\t` aligns the following text to the next tab stop; both are used by the factory defaults for the tax line and for right-aligned amounts.
- `spacerAfter` blank rows are printed with the smallest style (`FillerStyle`).

### Styles

| Style | Size | Typical use |
|-------|------|-------------|
| `LargeStyle` | 19 pt | Motel name |
| `HeaderStyle` | 10 pt | Titles |
| `DefaultStyle` | 10 pt | Body text |
| `TransactionStyle` | 9 pt | Transaction details |
| `FooterStyle` | 8 pt | Address and footer |
| `SecondLastStyle` | 7 pt | Fine print |
| `FillerStyle` | 1 pt | Blank spacer rows (`spacerAfter`) |

Adding `"bold": true` selects the bold variant. All styles use the Calibri family; the sizes above are the ones used on paper, at 100 % preview zoom.

---

## Available fields

### Every document

| Key | Label in the editor | Sample |
|-----|--------------------|--------|
| `motelName` | Nombre del motel | `MOTEL LAS PALMAS` |
| `motelAddress` | Dirección del motel | `Cra 10 #20-30` |
| `motelID` | NIT | `900.123.456-7` |
| `legalText` | Texto legal (IVA) | `  PERSONA NATURAL.\n NO RESPONSABLE DE IVA` |

### `ROOM_RECEIPT`

| Key | Label | Sample |
|-----|-------|--------|
| `consecutive` | No. de factura | `1024` |
| `roomString` | Habitación (completa) | `Habitación 3` |
| `roomNumber` | Número de habitación | `3` |
| `floorNumber` | Piso | `1` |
| `towerNumber` | Torre | `2` |
| `entryTime` | Hora de entrada | `08:30 PM` |
| `serviceDuration` | Duración del servicio | `3h` |
| `extensionDuration` | Extensión | `1h` |
| `totalPrice` | Pago total | `$40.000` |
| `date` | Fecha | `2026-09-15` |
| `reminderText` | Recordatorio | `NO OLVIDE SUS PERTENENCIAS` |

### `SALE_RECEIPT`

| Key | Label | Sample |
|-----|-------|--------|
| `consecutive` | No. de factura | `1024` |
| `roomSoldTo` | Habitación | `Habitación 3` |
| `saleTime` | Hora de venta | `08:30 PM` |
| `date` | Fecha | `2026-09-15` |
| `items` | Ítems vendidos (lista) | `   1 Cerveza\t$8.000` |
| `totalPrice` | Pago total | `$11.000` |

### `TURN_SUMMARY` and `TURN_DETAIL`

| Key | Label | Sample |
|-----|-------|--------|
| `turnNumber` | Número de turno | `42` |
| `turnStart` | Inicio de turno | `2026/09/15 - 20:30:00` |
| `turnEnd` | Fin de turno | `2026/09/16 - 05:30:00` |
| `totalRooms` | Total habitaciones | `$40.000` |
| `totalItems` | Total productos | `$11.000` |
| `totalSales` | Total ventas | `$51.000` |
| `totalRoomRefunds` | Reembolsos de habitaciones | `$0` |
| `totalItemRefunds` | Reembolsos de productos | `$0` |
| `totalRefunds` | Total reembolsos | `$0` |
| `totalSpending` | Total gastos | `$5.000` |
| `totalTurn` | Total turno | `$46.000` |
| `totalBankTransfers` | Transferencias | `$0` |
| `totalDeposits` | Depósitos | `$0` |
| `totalNet` | Total neto | `$46.000` |

`TURN_SUMMARY` also provides the two list fields below, and `TURN_DETAIL` the third one. List fields already hold one formatted row per entry, each ending in `\n` except the last, and must sit in a line of their own:

| Key | Label | Only in |
|-----|-------|---------|
| `summaryList` | Conceptos vendidos (lista) | `TURN_SUMMARY` |
| `refundList` | Conceptos reembolsados (lista) | `TURN_SUMMARY` |
| `activityList` | Actividades (lista) | `TURN_DETAIL` |

The sample column is exactly what the editor shows in its preview and what `IMPRIMIR PRUEBA` prints, so a layout can be checked on paper without a real transaction.

---

## Editing the files by hand

The editor is the only safe way to change a template, but the files are plain JSON and can be edited or copied between machines:

- Copying `data/printTemplates/` to another installation carries the custom layouts over.
- Deleting a file (or `BORRAR PLANTILLA`) returns that type to the built-in layout. The next time the editor opens that type it starts from the factory default again.
- A file that cannot be read (truncated, invalid JSON, unknown syntax) is reported in the application log and that type falls back to the built-in layout; nothing else is affected.
- Saving from the editor writes the file atomically (a `.tmp` file followed by a move), so a crash mid-save does not leave a half-written template.

The template files are not part of the turn data: they are never read back into the turn, the register or the history, and the JSON persistence schema is unchanged by them (see [Persistence Schema](PERSISTENCE/PERSISTENCE_SCHEMA.md)).
