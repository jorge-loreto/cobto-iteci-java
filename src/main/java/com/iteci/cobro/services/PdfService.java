package com.iteci.cobro.services;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.print.Book;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Date;

import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.MediaPrintableArea;
import javax.print.attribute.standard.PrintQuality;
import javax.print.attribute.standard.PrinterResolution;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;
import org.apache.pdfbox.printing.PDFPageable;
import org.apache.pdfbox.printing.PDFPrintable;
import org.apache.pdfbox.printing.Scaling;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.iteci.cobro.dto.AlumnoAsistenciaDTO;
import com.iteci.cobro.utils.NumeroALetrasUtil;
import com.iteci.cobro.entities.Perfil;
import com.iteci.cobro.exceptions.IteciPrinterException;
import com.iteci.cobro.repository.ListaAsistenciaRepository;
import com.iteci.cobro.repository.PerfilRepository;

@Service
public class PdfService {

    /**
     * Generates a half-letter PDF receipt for the given AlumnoAsistenciaDTO.
     * 
     * @param dto the receipt data
     * @return the generated PDF file
     * @throws IOException
     */

    @Autowired
    PerfilRepository perfilRepository;
    
    @Autowired
    private ListaAsistenciaRepository listaAsistenciaRepository;


    public File generateReciboPDF2(AlumnoAsistenciaDTO dto) throws IOException {
        PDDocument document = new PDDocument();

        // Half-letter page: 5.5 x 8.5 inches = 396 x 612 points
        PDRectangle halfLetter = new PDRectangle(396, 612);
        PDPage page = new PDPage(halfLetter);
        document.addPage(page);

        PDPageContentStream content = new PDPageContentStream(document, page);

        // Write receipt content
        content.beginText();
        content.setFont(PDType1Font.HELVETICA_BOLD, 16);
        content.newLineAtOffset(20, 580);
        content.showText("RECIBO DE PAGO");

        content.setFont(PDType1Font.HELVETICA, 12);
        content.newLineAtOffset(0, -40);

        content.showText("Alumno: " + dto.nombre() + " "
                + dto.apellidoPaterno() + " "
                + dto.apellidoMaterno());
        content.newLineAtOffset(0, -20);

        content.showText("Teléfono: " + dto.telefono());
        content.newLineAtOffset(0, -20);

        content.showText("Grupo: " + dto.idGrupo());
        content.newLineAtOffset(0, -20);

        content.showText("Día Semana: " + dto.diaSemana());
        content.newLineAtOffset(0, -20);

        content.showText("Hora Inicio: " + dto.horaInicio());
        content.newLineAtOffset(0, -20);

        content.showText("Modalidad: " + dto.modalidad());
        content.newLineAtOffset(0, -20);

        content.showText("Monto: $" + dto.monto());
        content.newLineAtOffset(0, -20);

        content.showText("Número Semana: " + dto.numeroSemana());
        content.newLineAtOffset(0, -20);

        content.showText("Folio: " + dto.folio());

        content.endText();
        content.close();

        // Save PDF
        String baseDir = System.getProperty("user.home") + "/recibos";
        File dir = new File(baseDir);
        if (!dir.exists()) {
            dir.mkdirs(); // create directory if not exist
        }
        String path = baseDir + "/recibo_" + dto.folio() + ".pdf";
        // String filePath = "~/recibos/recibo_" + dto.getFolio() + ".pdf";
        File file = new File(path);
        document.save(file);
        document.close();

        return file;
    }

    public void printPDF1(File pdfFile) throws Exception {
        PDDocument document = PDDocument.load(pdfFile);

        PrinterJob job = PrinterJob.getPrinterJob();
        job.setJobName("Recibo Pago");

        // Convert PDF pages to printable format
        PDFPrintable printable = new PDFPrintable(document, Scaling.SHRINK_TO_FIT);

        Book book = new Book();/*
                                * for (int i = 0; i < document.getNumberOfPages(); i++) {
                                * book.append(printable, PageFormatFactory.createPageFormat(), 1);
                                * }
                                */
        job.setPageable(book);

        // ❌ Remove printDialog() for headless / silent printing
        // job.printDialog();

        // ✅ Directly send to default printer
        job.print();

        document.close();
    }

    public File generateReciboPDF3(AlumnoAsistenciaDTO dto) throws IOException {
        PDDocument document = new PDDocument();

        // Load Ubuntu fonts
        PDFont ubuntu = PDType0Font.load(document, new File("src/main/resources/fonts/Ubuntu-Regular.ttf"));
        PDFont ubuntuBold = PDType0Font.load(document, new File("src/main/resources/fonts/Ubuntu-Bold.ttf"));

        // Half-letter page
        PDRectangle halfLetter = new PDRectangle(396, 612);
        PDPage page = new PDPage(halfLetter);
        document.addPage(page);

        PDPageContentStream content = new PDPageContentStream(document, page);

        // ---------------- LOGO ----------------
        try {
            PDImageXObject logo = PDImageXObject.createFromFile(
                    getClass().getClassLoader().getResource("iteci-logo.png").getPath(),
                    document);

            content.drawImage(logo, 20, 540, 60, 60);
        } catch (IOException e) {
            System.out.println("Logo not found.");
        }

        // ---------------- CAMPUS INFO ----------------
        content.beginText();
        content.setFont(ubuntuBold, 12);
        content.newLineAtOffset(120, 580);
        content.showText("CAMPUS DE BELLEZA ITECI");
        content.endText();

        content.beginText();
        content.setFont(ubuntu, 10);
        content.newLineAtOffset(120, 560);
        content.showText("Dirección: Av. Principal #123");
        content.endText();

        content.beginText();
        content.setFont(ubuntu, 10);
        content.newLineAtOffset(120, 545);
        content.showText("Tel: 444-123-4567");
        content.endText();

        // ---------------- SIGNATURE BOX ----------------
        content.setLineWidth(1.2f);
        content.addRect(280, 520, 90, 60);
        content.stroke();

        content.beginText();
        content.setFont(ubuntuBold, 9);
        content.newLineAtOffset(295, 550);
        content.showText("SELLO / FIRMA");
        content.endText();

        // ---------------- TITLE ----------------
        content.beginText();
        content.setFont(ubuntuBold, 16);
        content.newLineAtOffset(20, 480);
        content.showText("RECIBO DE PAGO");
        content.endText();

        // ---------------- BODY ----------------
        float y = 450;
        float lineSpacing = 18;

        content.beginText();
        content.setFont(ubuntu, 12);
        content.newLineAtOffset(20, y);

        content.showText("Alumno: " + dto.nombre() + " " + dto.apellidoPaterno() + " " + dto.apellidoMaterno());
        content.newLineAtOffset(0, -lineSpacing);

        content.showText("Teléfono: " + dto.telefono());
        content.newLineAtOffset(0, -lineSpacing);

        content.showText("Grupo: " + dto.idGrupo());
        content.newLineAtOffset(0, -lineSpacing);

        content.showText("Día Semana: " + dto.diaSemana());
        content.newLineAtOffset(0, -lineSpacing);

        content.showText("Hora Inicio: " + dto.horaInicio());
        content.newLineAtOffset(0, -lineSpacing);

        content.showText("Modalidad: " + dto.modalidad());
        content.newLineAtOffset(0, -lineSpacing);

        // ---------------- MONTO in RED + Ubuntu Bold ----------------
        content.setNonStrokingColor(200, 0, 0); // red
        content.setFont(ubuntuBold, 14);
        content.showText("Monto: $" + dto.monto());
        content.setNonStrokingColor(0, 0, 0); // reset to black
        content.setFont(ubuntu, 12);
        content.newLineAtOffset(0, -lineSpacing);

        content.showText("Número Semana: " + dto.numeroSemana());
        content.newLineAtOffset(0, -lineSpacing);

        content.showText("Folio: " + dto.folio());
        content.endText();

        content.close();

        // ---------------- SAVE FILE ----------------
        String baseDir = System.getProperty("user.home") + "/recibos";
        File dir = new File(baseDir);
        if (!dir.exists())
            dir.mkdirs();

        File file = new File(baseDir + "/recibo_" + dto.folio() + ".pdf");
        document.save(file);
        document.close();

        return file;
    }

    public void printPDF2(File pdfFile) throws Exception {
        PDDocument document = PDDocument.load(pdfFile);

        PrinterJob job = PrinterJob.getPrinterJob();
        job.setJobName("Recibo Pago");

        // ⬅ FIXED: print at actual size (no scaling)
        PDFPrintable printable = new PDFPrintable(document, Scaling.ACTUAL_SIZE);

        Book book = new Book();
        /*
         * for (int i = 0; i < document.getNumberOfPages(); i++) {
         * book.append(printable, PageFormatFactory.createPageFormat(), 1);
         * }
         */
        job.setPageable(book);

        // Direct print
        job.print();

        document.close();
    }
    public void printPDF(File pdfFile) throws IteciPrinterException {

        try (PDDocument document = PDDocument.load(pdfFile)) {

            PDFRenderer renderer = new PDFRenderer(document);
            PrinterJob job = PrinterJob.getPrinterJob();

            job.setPrintable((graphics, pageFormat, pageIndex) -> {
                if (pageIndex >= document.getNumberOfPages()) {
                    return Printable.NO_SUCH_PAGE;
                }

                Graphics2D g2d = (Graphics2D) graphics;

                try {
                    BufferedImage image = renderer.renderImageWithDPI(pageIndex, 300);

                    double xScale = pageFormat.getImageableWidth() / image.getWidth();
                    double yScale = pageFormat.getImageableHeight() / image.getHeight();
                    double scale = Math.min(xScale, yScale);

                    g2d.translate(
                            pageFormat.getImageableX(),
                            pageFormat.getImageableY());
                    g2d.scale(scale, scale);
                    g2d.drawImage(image, 0, 0, null);

                } catch (IOException e) {
                    throw new RuntimeException("Error rendering PDF page", e);
                }

                return Printable.PAGE_EXISTS;
            });

            job.print();

        }catch (FileNotFoundException e) {
            throw new IteciPrinterException("PDF file not found: " + pdfFile.getAbsolutePath(), e);

        }catch (IOException e) {
            throw new IteciPrinterException("Error loading PDF", e);

        }catch (PrinterException e) {
            throw new IteciPrinterException("Error printing PDF", e);

        }catch (Exception e) {
            throw new IteciPrinterException("Unexpected error while printing", e);
        }
    }
    //not used replced by printPDF() with better error handling and resource management 20260403
    public void printPDF23(File pdfFile) throws Exception {
    
        try (PDDocument document = PDDocument.load(pdfFile)) {

            PDFRenderer renderer = new PDFRenderer(document);

            PrinterJob job = PrinterJob.getPrinterJob();

            job.setPrintable((graphics, pageFormat, pageIndex) -> {
                if (pageIndex >= document.getNumberOfPages()) {
                    return Printable.NO_SUCH_PAGE;
                }

                Graphics2D g2d = (Graphics2D) graphics;

                BufferedImage image;
                try {
                    image = renderer.renderImageWithDPI(pageIndex, 300);
                } catch (IOException e) {
                    e.printStackTrace();
                    return Printable.NO_SUCH_PAGE;
                }

                double xScale = pageFormat.getImageableWidth() / image.getWidth();
                double yScale = pageFormat.getImageableHeight() / image.getHeight();
                double scale = Math.min(xScale, yScale);

                g2d.translate(
                        pageFormat.getImageableX(),
                        pageFormat.getImageableY());
                g2d.scale(scale, scale);
                g2d.drawImage(image, 0, 0, null);

                return Printable.PAGE_EXISTS;
            });

            job.print();
        }catch (FileNotFoundException e) {
            System.out.println("PDF file not found: " + pdfFile.getAbsolutePath());
        }catch (IOException e) {
            System.out.println("Error loading PDF: " + e.getMessage());
        }catch (PrinterException e) {
            System.out.println("Error printing PDF: " + e.getMessage());
        }catch (Exception e) {
            System.out.println("Unexpected error: " + e.getMessage());
        }
    }

    public void printPDFxyz2(File pdfFile) throws Exception {
        try (PDDocument document = PDDocument.load(pdfFile)) {

            PrinterJob job = PrinterJob.getPrinterJob();
            job.setJobName("ReciboColegiatura");

            PDFPrintable printable = new PDFPrintable(
                    document,
                    Scaling.ACTUAL_SIZE,
                    false,
                    720);

            // LETTER SIZE in points (1" = 72 points)
            double width = 8.5 * 72; // 612
            double height = 11 * 72; // 792

            // --- Epson L310 real minimal margins ≈ 3.0 – 3.5 mm ---
            double margin = 0.15 * 72; // 0.15 inch ≈ 3.8 mm (safe minimum)

            Paper paper = new Paper();
            paper.setSize(width, height);

            // imageable area with minimal margins (NO artificial Java margins)
            paper.setImageableArea(
                    margin, // left
                    margin, // top
                    width - (margin * 2), // printable width
                    height - (margin * 2) // printable height
            );

            PageFormat pf = job.defaultPage();
            pf.setPaper(paper);

            job.setPrintable(printable, pf);
            job.print();
        }
    }

    public File generateReciboPDF4(AlumnoAsistenciaDTO dto) throws IOException {
        PDDocument document = new PDDocument();

        // Full LETTER page (required for Epson borderless)
        PDPage page = new PDPage(PDRectangle.LETTER);
        document.addPage(page);

        PDPageContentStream content = new PDPageContentStream(document, page);

        float margin = 18; // 0.25 in
        float topY = 792 - margin; // LETTER height - margin

        content.beginText();
        content.setFont(PDType1Font.HELVETICA_BOLD, 18);
        content.newLineAtOffset(margin, topY);

        // Title
        content.showText("RECIBO DE PAGO");

        // Move down
        content.setFont(PDType1Font.HELVETICA, 12);
        content.newLineAtOffset(0, -40);

        content.showText("Alumno: " + dto.nombre() + " "
                + dto.apellidoPaterno() + " " + dto.apellidoMaterno());
        content.newLineAtOffset(0, -20);

        content.showText("Teléfono: " + dto.telefono());
        content.newLineAtOffset(0, -20);

        content.showText("Grupo: " + dto.idGrupo());
        content.newLineAtOffset(0, -20);

        content.showText("Día Semana: " + dto.diaSemana());
        content.newLineAtOffset(0, -20);

        content.showText("Hora Inicio: " + dto.horaInicio());
        content.newLineAtOffset(0, -20);

        content.showText("Modalidad: " + dto.modalidad());
        content.newLineAtOffset(0, -20);

        content.showText("Monto: $" + dto.monto());
        content.newLineAtOffset(0, -20);

        content.showText("Número Semana: " + dto.numeroSemana());
        content.newLineAtOffset(0, -20);

        content.showText("Folio: " + dto.folio());

        content.endText();
        content.close();

        // Save file
        String baseDir = System.getProperty("user.home") + "/recibos";
        File dir = new File(baseDir);
        if (!dir.exists())
            dir.mkdirs();

        File file = new File(baseDir + "/recibo_" + dto.folio() + ".pdf");
        document.save(file);
        document.close();

        return file;
    }

    public File generateReciboPDF5(AlumnoAsistenciaDTO dto) throws IOException {

        PDDocument document = new PDDocument();
        PDPage page = new PDPage(PDRectangle.LETTER); // required for borderless Epson
        document.addPage(page);

        PDPageContentStream content = new PDPageContentStream(document, page);

        float pageWidth = PDRectangle.LETTER.getWidth();
        float pageHeight = PDRectangle.LETTER.getHeight();
        float margin = 18; // 0.25 inch for content alignment
        float y = pageHeight - margin;

        // ===========================
        // 1) LOGO TOP-LEFT
        // ===========================
        InputStream logoStream = getClass().getResourceAsStream("/logo.png");
        if (logoStream != null) {
            PDImageXObject logo = PDImageXObject.createFromByteArray(document, logoStream.readAllBytes(), "logo");
            content.drawImage(logo, margin, y - 60, 80, 60); // (x, y, width, height)
        }

        // ===========================
        // 2) CAMPUS INFO (TOP-RIGHT)
        // ===========================
        content.beginText();
        content.setFont(PDType1Font.HELVETICA_BOLD, 14);
        content.newLineAtOffset(pageWidth - 250, y - 20);

        content.showText("Instituto de Capacitación");
        content.newLineAtOffset(0, -18);

        content.setFont(PDType1Font.HELVETICA, 12);
        content.showText("Campus Centro");
        content.newLineAtOffset(0, -16);
        content.showText("Dirección: Av. Principal #123");
        content.newLineAtOffset(0, -16);
        content.showText("Tel: (555) 123-4567");
        content.endText();

        y -= 90; // move below header

        // ===========================
        // 3) TITLE
        // ===========================
        content.beginText();
        content.setFont(PDType1Font.HELVETICA_BOLD, 20);
        content.newLineAtOffset(margin, y);
        content.showText("RECIBO DE PAGO");
        content.endText();

        y -= 30;

        // ===========================
        // 4) TABLE OF DATA
        // ===========================

        float tableX = margin;
        float tableY = y;
        float tableWidth = pageWidth - margin * 2;
        float rowHeight = 22;

        // Rows: label + value
        String[][] rows = {
                { "Alumno", dto.nombre() + " " + dto.apellidoPaterno() + " " + dto.apellidoMaterno() },
                { "Teléfono", dto.telefono() },
                { "Grupo", dto.idGrupo().toString() },
                { "Día Semana", dto.diaSemana() },
                { "Hora Inicio", dto.horaInicio() },
                { "Modalidad", dto.modalidad() },
                { "Monto", "$" + dto.monto() },
                { "Semana", dto.numeroSemana().toString() },
                { "Folio", dto.folio().toString() }
        };

        // Draw table lines + text
        for (String[] row : rows) {
            // Draw border
            content.setLineWidth(0.7f);
            content.addRect(tableX, tableY - rowHeight, tableWidth, rowHeight);
            content.stroke();

            // Label (left)
            content.beginText();
            content.setFont(PDType1Font.HELVETICA_BOLD, 12);
            content.newLineAtOffset(tableX + 6, tableY - rowHeight + 6);
            content.showText(row[0] + ":");
            content.endText();

            // Value (right side)
            content.beginText();
            content.setFont(PDType1Font.HELVETICA, 12);
            content.newLineAtOffset(tableX + 150, tableY - rowHeight + 6);
            content.showText(row[1]);
            content.endText();

            tableY -= rowHeight;
        }

        y = tableY - 40; // move below table

        // ===========================
        // 5) SIGNATURE + SEAL BOX
        // ===========================

        float boxWidth = 250;
        float boxHeight = 100;
        float boxX = pageWidth - boxWidth - margin;
        float boxY = y;

        // Draw box
        content.setLineWidth(1f);
        content.addRect(boxX, boxY, boxWidth, boxHeight);
        content.stroke();

        // Label inside
        content.beginText();
        content.setFont(PDType1Font.HELVETICA, 12);
        content.newLineAtOffset(boxX + 20, boxY + boxHeight - 20);
        content.showText("Firma y Sello");
        content.endText();

        content.close();

        // Save file
        String baseDir = System.getProperty("user.home") + "/recibos";
        File dir = new File(baseDir);
        if (!dir.exists())
            dir.mkdirs();

        File file = new File(baseDir + "/recibo_" + dto.folio() + ".pdf");
        document.save(file);
        document.close();

        return file;
    }

    public File generateReciboPDF(AlumnoAsistenciaDTO dto) throws IOException {

        PDDocument document = new PDDocument();
        PDPage page = new PDPage(PDRectangle.LETTER);
        document.addPage(page);

        float pageWidth = PDRectangle.LETTER.getWidth();
        float pageHeight = PDRectangle.LETTER.getHeight();
        float margin = 18;
        float y = pageHeight - margin;

        // ===========================
        // 1) WATERMARK (PREPEND)
        // ===========================
        try (PDPageContentStream bg = new PDPageContentStream(
                document, page, PDPageContentStream.AppendMode.PREPEND, false)) {

            InputStream logoStream = getClass().getResourceAsStream("/logo.png");

            if (logoStream != null) {
                PDImageXObject logo = PDImageXObject.createFromByteArray(
                        document, logoStream.readAllBytes(), "logo");

                float naturalW = logo.getWidth();
                float naturalH = logo.getHeight();

                float scale = 1.5f;
                float logoW = naturalW * scale;
                float logoH = naturalH * scale;

                float maxW = pageWidth * 0.6f;
                if (logoW > maxW) {
                    float reduce = maxW / logoW;
                    logoW *= reduce;
                    logoH *= reduce;
                }

                // TOP-CENTER
                float posX = (pageWidth - logoW) / 2f;
                float posY = pageHeight - logoH - 40;

                // faded watermark only for image
                bg.saveGraphicsState();
                PDExtendedGraphicsState gs = new PDExtendedGraphicsState();
                gs.setNonStrokingAlphaConstant(0.15f);
                bg.setGraphicsStateParameters(gs);

                bg.drawImage(logo, posX, posY, logoW, logoH);

                bg.restoreGraphicsState();
            }
        }

        // ======================================
        // 2) MAIN CONTENT LAYER
        // ======================================
        PDPageContentStream content = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND,
                false);

        // HEADER BLOCK
        content.beginText();
        content.setFont(PDType1Font.HELVETICA_BOLD, 14);
        content.newLineAtOffset(pageWidth - 250, y - 20);
        content.showText("Instituto de Capacitación");
        content.newLineAtOffset(0, -18);
        content.setFont(PDType1Font.HELVETICA, 12);
        content.showText("Campus Centro");
        content.newLineAtOffset(0, -16);
        content.showText("Dirección: Av. Principal #123");
        content.newLineAtOffset(0, -16);
        content.showText("Tel: (555) 123-4567");
        content.endText();

        y -= 60;

        // TITLE
        content.beginText();
        content.setFont(PDType1Font.HELVETICA_BOLD, 30);
        content.newLineAtOffset(margin, y);
        content.showText("FOLIO: #" + dto.folio().toString());
        content.endText();

        y -= 90;

        // TITLE
        content.beginText();
        content.setFont(PDType1Font.HELVETICA_BOLD, 20);
        content.newLineAtOffset(margin, y);
        content.showText("RECIBO DE PAGO");
        content.endText();

        y -= 30;

        // ======================================
        // TABLE (NOW 75% WIDTH)
        // ======================================
        float tableX = margin;
        float tableY = y;

        float tableWidth = (pageWidth - margin * 2) * 0.75f; // <-- 75%

        float rowHeight = 22;

        String[][] rows = {
                { "Alumno", dto.nombre() + " " + dto.apellidoPaterno() + " " + dto.apellidoMaterno() },
                { "Teléfono", dto.telefono() },
                { "Grupo", dto.idGrupo().toString() },
                { "Día Semana", dto.diaSemana() },
                { "Hora Inicio", dto.horaInicio() },
                { "Modalidad", dto.modalidad() },
                { "Monto", "$" + dto.monto() },
                { "Semana", dto.numeroSemana().toString() },
                { "Folio", dto.folio().toString() }
        };

        for (String[] row : rows) {

            // row box
            content.setLineWidth(0.7f);
            content.addRect(tableX, tableY - rowHeight, tableWidth, rowHeight);
            content.stroke();

            // label
            content.beginText();
            content.setFont(PDType1Font.HELVETICA_BOLD, 12);
            content.newLineAtOffset(tableX + 6, tableY - rowHeight + 6);
            content.showText(row[0] + ":");
            content.endText();

            // value
            content.beginText();
            content.setFont(PDType1Font.HELVETICA, 12);
            content.newLineAtOffset(tableX + 150, tableY - rowHeight + 6);
            content.showText(row[1]);
            content.endText();

            tableY -= rowHeight;
        }

        y = tableY - 40;

        // ======================================
        // SIGNATURE BOX (RIGHT OF TABLE)
        // ======================================
        float boxWidth = pageWidth - margin - (tableX + tableWidth) - 20; // auto-fit right side
        float boxHeight = 100;

        float boxX = tableX + tableWidth + 20;
        float boxY = y;

        content.setLineWidth(1f);
        content.addRect(boxX, boxY, boxWidth, boxHeight);
        content.stroke();

        content.beginText();
        content.setFont(PDType1Font.HELVETICA, 12);
        content.newLineAtOffset(boxX + 20, boxY + boxHeight - 20);
        content.showText("Firma y Sello");
        content.endText();

        // CLOSE MAIN CONTENT STREAM
        content.close();

        // SAVE FILE
        String baseDir = System.getProperty("user.home") + "/recibos";
        File dir = new File(baseDir);
        if (!dir.exists())
            dir.mkdirs();

        File file = new File(baseDir + "/recibo_" + dto.folio() + ".pdf");
        document.save(file);

        document.close();
        return file;
    }

    public String formatTelefono(String telefono) {
        if (telefono == null)
            return "";

        // Keep only digits
        String digits = telefono.replaceAll("\\D", "");

        // Expecting 10 digits: AAA BBB CC DD
        if (digits.length() != 10)
            return telefono; // fallback

        return String.format("(%s)%s-%s-%s",
                digits.substring(0, 3), // area code
                digits.substring(3, 6), // first three
                digits.substring(6, 8), // next two
                digits.substring(8, 10)); // last two
    }

    public String formatLocation(Perfil perfil) {
        String localidad = perfil.getLocalidad();
        String municipio = perfil.getMunicipio();
        String entidad = perfil.getEntidad();

        StringBuilder sb = new StringBuilder();

        // if localidad == municipio → only print once
        if (localidad != null && localidad.equalsIgnoreCase(municipio)) {
            sb.append(localidad);
        } else {
            sb.append(localidad);
            if (municipio != null && !municipio.isEmpty()) {
                sb.append(", ").append(municipio);
            }
        }

        if (entidad != null && !entidad.isEmpty()) {
            sb.append(", ").append(entidad);
        }

        return sb.toString();
    }

    public File generateReciboPDFxyz(AlumnoAsistenciaDTO dto) throws IOException {

        Perfil perfil = perfilRepository.findById(1L).orElse(
                Perfil.builder()
                        .idPerfil(1L)
                        .nombrePerfil("Grupo ITECI")
                        .clave("01CIGHHJDFREWSA")
                        .entidad("Aguascalientes")
                        .municipio("Aguascalientes")
                        .localidad("Aguascalientes")
                        .direccion("Av. Aguascalientes #123")
                        .colonia("Centro")
                        .telefono("449-123-4455")
                        .build());

        PDDocument document = new PDDocument();
        PDPage page = new PDPage(PDRectangle.LETTER);
        document.addPage(page);

        float pageWidth = PDRectangle.LETTER.getWidth();
        float pageHeight = PDRectangle.LETTER.getHeight();
        float margin = 18;
        float y = pageHeight - margin;

        // ===========================
        // LOAD UBUNTU FONTS
        // ===========================
        PDFont ubuntu = PDType0Font.load(document, new File("src/main/resources/fonts/Ubuntu-Regular.ttf"));
        PDFont ubuntuBold = PDType0Font.load(document, new File("src/main/resources/fonts/Ubuntu-Bold.ttf"));

        // ===========================
        // 1) WATERMARK (IMAGE ONLY)
        // ===========================
        try (PDPageContentStream bg = new PDPageContentStream(
                document, page, PDPageContentStream.AppendMode.PREPEND, false)) {

            // ---------------- LOGO ----------------
            PDImageXObject logo = null;
            try {
                logo = PDImageXObject.createFromFile(
                        getClass().getClassLoader().getResource("logo.png").getPath(),
                        document);
            } catch (IOException e) {
                System.out.println("Logo not found.");
            }

            float naturalW = logo.getWidth();
            float naturalH = logo.getHeight();

            float scale = 1.5f;
            float logoW = naturalW * scale;
            float logoH = naturalH * scale;

            float maxW = pageWidth * 0.6f;
            if (logoW > maxW) {
                float reduce = maxW / logoW;
                logoW *= reduce;
                logoH *= reduce;
            }

            float posX = (pageWidth - logoW) / 2f;
            float posY = pageHeight - logoH - 40;

            bg.saveGraphicsState();
            PDExtendedGraphicsState gs = new PDExtendedGraphicsState();
            gs.setNonStrokingAlphaConstant(0.15f);
            bg.setGraphicsStateParameters(gs);

            bg.drawImage(logo, posX, posY, logoW, logoH);
            bg.restoreGraphicsState();

        }

        // ================================
        // 2) MAIN CONTENT (APPEND)
        // ================================
        PDPageContentStream content = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND,
                false);

        // HEADER
        content.beginText();
        content.setFont(ubuntuBold, 14);
        content.newLineAtOffset(pageWidth - 250, y - 20);
        content.showText(perfil.getNombrePerfil());
        content.newLineAtOffset(0, -18);
        content.setFont(ubuntu, 12);
        content.showText("Campus: " + perfil.getLocalidad());
        content.newLineAtOffset(0, -16);
        content.showText("Dirección: " + perfil.getDireccion() + ", " + perfil.getColonia());
        content.newLineAtOffset(0, -16);
        content.showText(formatLocation(perfil));
        content.newLineAtOffset(0, -16);
        content.showText("Tel: " + formatTelefono(perfil.getTelefono()));
        content.endText();

        y -= 20;

        // TITLE
        content.beginText();
        content.setFont(ubuntuBold, 30);
        content.newLineAtOffset(margin, y);
        content.showText("RECIBO DE PAGO");
        content.endText();

        y -= 90;

        // TITLE
        content.beginText();
        content.setFont(ubuntuBold, 20);
        content.newLineAtOffset(margin, y);
        content.showText("FOLIO #" + dto.folio().toString());
        content.endText();

        y -= 30;

        // ======================
        // TABLE (75% WIDTH)
        // ======================
        float tableX = margin;
        float tableWidth = pageWidth * 0.75f; // ← 75%
        float tableY = y;
        float rowHeight = 24;
        float radius = 6;

        String[][] rows = {
                { "Alumno", dto.nombre() + " " + dto.apellidoPaterno() + " " + dto.apellidoMaterno() },
                { "Curso", dto.modalidad() },
                { "Horario ", dto.diaSemana() + " " + dto.horaInicio() + " - " + dto.horaFinal() },

                { "Fecha de pago", dto.fechaPago().toString() },
                { "Total", "$" + dto.monto() + " " + NumeroALetrasUtil.convertirMontoEnLetras(dto.monto()) },
                { "Concepto de pago", "Colegiatura semanal #" + dto.numeroSemana().toString() },

        };

        for (String[] row : rows) {

            // Rounded rectangle row
            drawRoundedRect(content, tableX, tableY - rowHeight, tableWidth, rowHeight, radius);
            content.stroke();

            // Header text
            content.beginText();
            content.setFont(ubuntuBold, 12);
            content.newLineAtOffset(tableX + 8, tableY - rowHeight + 7);
            content.showText(row[0] + ":");
            content.endText();

            // Value text
            content.beginText();
            content.setFont(ubuntu, 12);
            content.newLineAtOffset(tableX + 150, tableY - rowHeight + 7);
            content.showText(row[1]);
            content.endText();

            tableY -= rowHeight + 4;
        }

        y = tableY - 40;

        // ================================
        // SIGNATURE BOX WITH ROUNDED CORNERS
        // ================================
        float boxWidth = 100;
        float boxHeight = 100;
        float boxX = pageWidth - boxWidth - margin;
        float boxY = y;

        drawRoundedRect(content, boxX, boxY, boxWidth, boxHeight, 10);
        content.stroke();

        content.beginText();
        content.setFont(ubuntu, 12);
        content.newLineAtOffset(boxX + 20, boxY + boxHeight - 22);
        content.showText("Firma y Sello");
        content.endText();

        content.close();

        // SAVE FILE
        String baseDir = System.getProperty("user.home") + "/recibos";
        File dir = new File(baseDir);
        if (!dir.exists())
            dir.mkdirs();

        File file = new File(baseDir + "/recibo_" + dto.folio() + ".pdf");
        document.save(file);
        document.close();
        return file;
    }

    private void drawRoundedRect(PDPageContentStream content, float x, float y, float width, float height, float r)
            throws IOException {
        float k = 0.55228475f; // control point constant for circle approximation

        content.moveTo(x + r, y);
        content.lineTo(x + width - r, y);

        content.curveTo(x + width - r + r * k, y,
                x + width, y + r - r * k,
                x + width, y + r);

        content.lineTo(x + width, y + height - r);

        content.curveTo(x + width, y + height - r + r * k,
                x + width - r + r * k, y + height,
                x + width - r, y + height);

        content.lineTo(x + r, y + height);

        content.curveTo(x + r - r * k, y + height,
                x, y + height - r + r * k,
                x, y + height - r);

        content.lineTo(x, y + r);

        content.curveTo(x, y + r - r * k,
                x + r - r * k, y,
                x + r, y);

        content.closePath();
    }

    public File generateReciboPDFxyz9(AlumnoAsistenciaDTO dto) throws IOException {

        Perfil perfil = perfilRepository.findById(1L).orElse(
                Perfil.builder()
                        .idPerfil(1L)
                        .nombrePerfil("Grupo ITECI")
                        .clave("01CIGHHJDFREWSA")
                        .entidad("Aguascalientes")
                        .municipio("Aguascalientes")
                        .localidad("Aguascalientes")
                        .direccion("Av. Aguascalientes #123")
                        .colonia("Centro")
                        .telefono("449-123-4455")
                        .build());

        PDDocument document = new PDDocument();
        PDPage page = new PDPage(PDRectangle.LETTER);
        document.addPage(page);

        float pageWidth = PDRectangle.LETTER.getWidth();
        float pageHeight = PDRectangle.LETTER.getHeight();
        float margin = 18;
        float y = pageHeight - margin;

        // ===========================
        // LOAD UBUNTU FONTS
        // ===========================
        PDFont ubuntu = PDType0Font.load(document, new File("src/main/resources/fonts/Ubuntu-Regular.ttf"));
        PDFont ubuntuBold = PDType0Font.load(document, new File("src/main/resources/fonts/Ubuntu-Bold.ttf"));

        // ===========================
        // 1) WATERMARK (IMAGE ONLY)
        // ===========================
        try (PDPageContentStream bg = new PDPageContentStream(
                document, page, PDPageContentStream.AppendMode.PREPEND, false)) {

            // ---------------- LOGO ----------------
            PDImageXObject logo = null;
            try {
                logo = PDImageXObject.createFromFile(
                        getClass().getClassLoader().getResource("logo.png").getPath(),
                        document);
            } catch (IOException e) {
                System.out.println("Logo not found.");
            }

            float naturalW = logo.getWidth();
            float naturalH = logo.getHeight();

            float scale = 1.5f;
            float logoW = naturalW * scale;
            float logoH = naturalH * scale;

            float maxW = pageWidth * 0.6f;
            if (logoW > maxW) {
                float reduce = maxW / logoW;
                logoW *= reduce;
                logoH *= reduce;
            }

            float posX = (pageWidth - logoW) / 2f;
            float posY = pageHeight - logoH - 40;

            bg.saveGraphicsState();
            PDExtendedGraphicsState gs = new PDExtendedGraphicsState();
            gs.setNonStrokingAlphaConstant(0.15f);
            bg.setGraphicsStateParameters(gs);

            bg.drawImage(logo, posX, posY, logoW, logoH);
            bg.restoreGraphicsState();

        }

        // ================================
        // 2) MAIN CONTENT (APPEND)
        // ================================
        PDPageContentStream content = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND,
                false);

        // HEADER
        content.beginText();
        content.setFont(ubuntuBold, 14);
        content.newLineAtOffset(pageWidth - 250, y - 20);
        content.showText(perfil.getNombrePerfil());
        content.newLineAtOffset(0, -18);
        content.setFont(ubuntu, 12);
        content.showText("Campus: " + perfil.getLocalidad());
        content.newLineAtOffset(0, -16);
        content.showText("Dirección: " + perfil.getDireccion() + ", " + perfil.getColonia());
        content.newLineAtOffset(0, -16);
        content.showText(formatLocation(perfil));
        content.newLineAtOffset(0, -16);
        content.showText("Tel: " + formatTelefono(perfil.getTelefono()));
        content.endText();

        y -= 20;

        // TITLE
        content.beginText();
        content.setFont(ubuntuBold, 30);
        content.newLineAtOffset(margin, y);
        content.showText("RECIBO DE PAGO");
        content.endText();

        y -= 90;
        // adding logo real
        try {
            PDImageXObject logoxyz = PDImageXObject.createFromFile(
                    getClass().getClassLoader().getResource("logo.png").getPath(),
                    document);

            // Determine desired size (adjust as needed)
            float logoMaxWidth = 120; // points
            float scale = logoMaxWidth / logoxyz.getWidth();
            float logoW = logoxyz.getWidth() * scale;
            float logoH = logoxyz.getHeight() * scale;

            // Position: center horizontally, vertically in the space between title and
            // folio
            float logoX = (pageWidth - logoW) / 2f;
            float logoY = y - 60; // approximate vertical position, adjust to fit visually

            // Draw logo
            content.drawImage(logoxyz, logoX, logoY, logoW, logoH);

        } catch (Exception e) {
            System.out.println("Logo not found.");
        }

        // TITLE finishinglogo real
        content.beginText();
        content.setFont(ubuntuBold, 20);
        content.newLineAtOffset(margin, y);
        content.showText("FOLIO #" + dto.folio().toString());
        content.endText();

        y -= 30;

        // ======================
        // TABLE (75% WIDTH)
        // ======================
        float tableX = margin;
        float tableWidth = pageWidth * 0.75f; // ← 75%
        float tableY = y;
        float rowHeight = 24;
        float radius = 6;

        String[][] rows = {
                { "Alumno", dto.nombre() + " " + dto.apellidoPaterno() + " " + dto.apellidoMaterno() },
                { "Curso", dto.modalidad() },
                { "Horario ", dto.diaSemana() + " " + dto.horaInicio() + " - " + dto.horaFinal() },

                { "Fecha de pago", dto.fechaPago().toString() },
                { "Total", "$" + dto.monto() + " " + NumeroALetrasUtil.convertirMontoEnLetras(dto.monto()) },
                { "Concepto de pago", "Colegiatura semanal #" + dto.numeroSemana().toString() },

        };

        for (String[] row : rows) {

            // Rounded rectangle row
            drawRoundedRect(content, tableX, tableY - rowHeight, tableWidth, rowHeight, radius);
            content.stroke();

            // Header text
            content.beginText();
            content.setFont(ubuntuBold, 12);
            content.newLineAtOffset(tableX + 8, tableY - rowHeight + 7);
            content.showText(row[0] + ":");
            content.endText();

            // Value text
            content.beginText();
            content.setFont(ubuntu, 12);
            content.newLineAtOffset(tableX + 150, tableY - rowHeight + 7);
            content.showText(row[1]);
            content.endText();

            tableY -= rowHeight + 4;
        }

        y = tableY - 40;

        // ================================
        // SIGNATURE BOX WITH ROUNDED CORNERS
        // ================================
        float boxWidth = 100;
        float boxHeight = 100;
        float boxX = pageWidth - boxWidth - margin;
        float boxY = y;

        drawRoundedRect(content, boxX, boxY, boxWidth, boxHeight, 10);
        content.stroke();

        content.beginText();
        content.setFont(ubuntu, 12);
        content.newLineAtOffset(boxX + 20, boxY + boxHeight - 22);
        content.showText("Firma y Sello");
        content.endText();

        content.close();

        // SAVE FILE
        String baseDir = System.getProperty("user.home") + "/recibos";
        File dir = new File(baseDir);
        if (!dir.exists())
            dir.mkdirs();

        File file = new File(baseDir + "/recibo_" + dto.folio() + ".pdf");
        document.save(file);
        document.close();
        return file;
    }

    public File generateReciboPDFxyz99(AlumnoAsistenciaDTO dto) throws IOException {

        Perfil perfil = perfilRepository.findById(1L).orElse(
                Perfil.builder()
                        .idPerfil(1L)
                        .nombrePerfil("Grupo ITECI")
                        .clave("01CIGHHJDFREWSA")
                        .entidad("Aguascalientes")
                        .municipio("Aguascalientes")
                        .localidad("Aguascalientes")
                        .direccion("Av. Aguascalientes #123")
                        .colonia("Centro")
                        .telefono("449-123-4455")
                        .build());

        PDDocument document = new PDDocument();
        PDPage page = new PDPage(PDRectangle.LETTER);
        document.addPage(page);

        float pageWidth = PDRectangle.LETTER.getWidth();
        float pageHeight = PDRectangle.LETTER.getHeight();
        float margin = 18;

        PDFont ubuntu;
        try (InputStream regularFont = getClass().getResourceAsStream("/fonts/Ubuntu-Regular.ttf")) {
            ubuntu = PDType0Font.load(document, regularFont);
        }

        PDFont ubuntuBold;
        try (InputStream boldFont = getClass().getResourceAsStream("/fonts/Ubuntu-Bold.ttf")) {
            ubuntuBold = PDType0Font.load(document, boldFont);
        }

        PDPageContentStream content = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND,
                false);

        float logoWidth = 100;
        float logoHeight = 0;

        try {
            InputStream logoStream = getClass().getClassLoader().getResourceAsStream("logo.png");
            if (logoStream == null) {
                throw new FileNotFoundException("logo.png not found in resources");
            }

            PDImageXObject logo = PDImageXObject.createFromByteArray(
                    document,
                    logoStream.readAllBytes(),
                    "logo");

            float scale = logoWidth / logo.getWidth();
            logoHeight = logo.getHeight() * scale;
            float logoY = pageHeight - margin - logoHeight;

            content.drawImage(logo, margin, logoY, logoWidth, logoHeight);

            // TITLE CENTERED
            String titleText = "COMPROBANTE DE COLEGIATURA";
            content.beginText();
            content.setFont(ubuntuBold, 18);

            float titleWidth = ubuntuBold.getStringWidth(titleText) / 1000 * 24;
            float titleX = (pageWidth - titleWidth) / 2f;
            float titleY = pageHeight - margin - (logoHeight / 2) + 20;

            content.newLineAtOffset(titleX + 10, titleY);
            content.showText(titleText);
            content.endText();
            // folio
            content.beginText();
            content.setFont(ubuntuBold, 15);
            content.setNonStrokingColor(255, 100, 100); // red color for folio
                        
            content.newLineAtOffset((pageWidth - (margin)) - 180, titleY);
            content.showText(" : FOLIO # " + dto.folio().toString());
            content.endText();
            // end folio
            // Address small line under title
            titleY -= 24;
            content.beginText();
            content.setFont(ubuntu, 8);
            content.newLineAtOffset(titleX + 10, titleY + 10);
            content.showText(perfil.getDireccion() + ", " +
                    perfil.getColonia() + ", " + formatLocation(perfil) +
                    ", Tel: " + formatTelefono(perfil.getTelefono()));
            content.endText();

            // SCHOOL DESCRIPTION
            float schoolStartY = pageHeight - margin - logoHeight + 40;

            content.beginText();
            content.setFont(ubuntuBold, 14);
            content.newLineAtOffset(titleX + 10, schoolStartY);
            content.showText(perfil.getNombrePerfil());

            content.newLineAtOffset(0, -18);
            content.setFont(ubuntu, 12);
            content.showText("Campus: " + perfil.getLocalidad());
            content.endText();
            // ===========================
            // 4) MAIN CONTENT TABLE
            // ===========================

            // TABLE (just below Campus)
            float tableStartY = schoolStartY - 30;
            float tableX = margin;
            float tableWidth = pageWidth * 0.5f;
            float tableY = tableStartY;
            float rowHeight = 24;
            float radius = 6;

            String[][] rows = {
                    { "Alumno", dto.nombre() + " " + dto.apellidoPaterno() + " " + dto.apellidoMaterno() },
                    { "Curso", dto.modalidad() },
                    { "Horario", dto.diaSemana() + " " + dto.horaInicio() + " - " + dto.horaFinal() },
                    { "Fecha de pago", dto.fechaPago().toString() },
                    { "Total", "$" + dto.monto() },
                    { "Concepto de pago", "Colegiatura semanal # " + dto.numeroSemana().toString() },
                    { "Observaciones", dto.observaciones() },
                    { "Semana actual ", "#" + dto.semanaActual().toString() }
            };
            int count = 1;
            for (String[] row : rows) {
                drawRoundedRect(content, tableX, tableY - rowHeight, tableWidth, rowHeight, radius);
                content.stroke();

                // --- LABEL (Left column) ---
                content.beginText();
                content.setFont(ubuntuBold, 12);
                content.newLineAtOffset(tableX + 8, tableY - rowHeight + 7);
                content.showText(row[0] + ":");
                content.endText();

                // --- VALUE (Right column) ---
                content.beginText();
                content.setFont(ubuntu, 12);
                if (count < 4) {
                    count++;
                    content.newLineAtOffset(tableX + 60, tableY - rowHeight + 7);
                } else
                    content.newLineAtOffset(tableX + 125, tableY - rowHeight + 7);
                content.showText(row[1]);
                content.endText();

                // --- VALUE IN LETTERS (only for money rows) ---
                if (row[1].startsWith("$")) {
                    content.beginText();
                    content.setFont(ubuntu, 8);
                    content.newLineAtOffset(tableX + 155, tableY - rowHeight + 7); // slightly lower
                    content.showText("(" + NumeroALetrasUtil.convertirMontoEnLetras(dto.monto()) + ")");
                    content.endText();
                }

                tableY -= rowHeight + 4;
            }

            float boxWidth = 100;
            float boxHeight = 100;
            float boxX = pageWidth - boxWidth - margin;
            float boxY = tableY;

            drawRoundedRect(content, boxX - 30, boxY, boxWidth, boxHeight, 10);
            content.stroke();

            content.beginText();
            content.setFont(ubuntu, 12);
            content.newLineAtOffset(boxX - 20, boxY + boxHeight - 22);
            content.showText("Firma y Sello");
            content.endText();

            content.close();

            String baseDir = System.getProperty("user.home") + "/recibos";
            File dir = new File(baseDir);
            if (!dir.exists())
                dir.mkdirs();

            File file = new File(baseDir + "/" + dto.folio() + ".pdf");
            document.save(file);
            document.close();
            return file;

        } catch (IOException e) {
            e.printStackTrace();
            content.close();
            document.close();
            return null;
        }
    }

    public File generateReciboPDFxyz99ModifiedAmount(AlumnoAsistenciaDTO dto) throws IOException {

        Perfil perfil = perfilRepository.findById(1L).orElse(
                Perfil.builder()
                        .idPerfil(1L)
                        .nombrePerfil("Grupo ITECI")
                        .clave("01CIGHHJDFREWSA")
                        .entidad("Aguascalientes")
                        .municipio("Aguascalientes")
                        .localidad("Aguascalientes")
                        .direccion("Av. Aguascalientes #123")
                        .colonia("Centro")
                        .telefono("449-123-4455")
                        .build());

        PDDocument document = new PDDocument();
        PDPage page = new PDPage(PDRectangle.LETTER);
        document.addPage(page);

        float pageWidth = PDRectangle.LETTER.getWidth();
        float pageHeight = PDRectangle.LETTER.getHeight();
        float margin = 18;

        PDFont ubuntu;
        try (InputStream regularFont = getClass().getResourceAsStream("/fonts/Ubuntu-Regular.ttf")) {
            ubuntu = PDType0Font.load(document, regularFont);
        }

        PDFont ubuntuBold;
        try (InputStream boldFont = getClass().getResourceAsStream("/fonts/Ubuntu-Bold.ttf")) {
            ubuntuBold = PDType0Font.load(document, boldFont);
        }

        PDPageContentStream content = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND,
                false);

        float logoWidth = 100;
        float logoHeight = 0;

        try {
            InputStream logoStream = getClass().getClassLoader().getResourceAsStream("logo.png");
            if (logoStream == null) {
                throw new FileNotFoundException("logo.png not found in resources");
            }

            PDImageXObject logo = PDImageXObject.createFromByteArray(
                    document,
                    logoStream.readAllBytes(),
                    "logo");

            float scale = logoWidth / logo.getWidth();
            logoHeight = logo.getHeight() * scale;
            float logoY = pageHeight - margin - logoHeight;

            content.drawImage(logo, margin, logoY, logoWidth, logoHeight);

            // TITLE CENTERED
            String titleText = "COMPROBANTE DE COLEGIATURA";
            content.beginText();
            content.setFont(ubuntuBold, 18);

            float titleWidth = ubuntuBold.getStringWidth(titleText) / 1000 * 24;
            float titleX = (pageWidth - titleWidth) / 2f;
            float titleY = pageHeight - margin - (logoHeight / 2) + 20;

            content.newLineAtOffset(titleX + 10, titleY);
            content.showText(titleText);
            content.endText();
            // folio
            content.beginText();
            content.setFont(ubuntuBold, 15);
            content.newLineAtOffset((pageWidth - (margin)) - 180, titleY);
            content.showText(" : FOLIO # " + dto.folio().toString());
            content.endText();
            // end folio
            // Address small line under title
            titleY -= 24;
            content.beginText();
            content.setFont(ubuntu, 8);
            content.newLineAtOffset(titleX + 10, titleY + 10);
            content.showText(perfil.getDireccion() + ", " +
                    perfil.getColonia() + ", " + formatLocation(perfil) +
                    ", Tel: " + formatTelefono(perfil.getTelefono()));
            content.endText();

            // SCHOOL DESCRIPTION
            float schoolStartY = pageHeight - margin - logoHeight + 40;

            content.beginText();
            content.setFont(ubuntuBold, 14);
            content.newLineAtOffset(titleX + 10, schoolStartY);
            content.showText(perfil.getNombrePerfil());

            content.newLineAtOffset(0, -18);
            content.setFont(ubuntu, 12);
            content.showText("Campus: " + perfil.getLocalidad());
            content.endText();
            // ===========================
            // 4) MAIN CONTENT TABLE
            // ===========================

            // TABLE (just below Campus)
            float tableStartY = schoolStartY - 30;
            float tableX = margin;
            float tableWidth = pageWidth * 0.5f;
            float tableY = tableStartY;
            float rowHeight = 24;
            float radius = 6;

            String[][] rows = {
                    { "Alumno", dto.nombre() + " " + dto.apellidoPaterno() + " " + dto.apellidoMaterno() },
                    { "Curso", dto.modalidad() },
                    { "Horario", dto.diaSemana() + " " + dto.horaInicio() + " - " + dto.horaFinal() },
                    { "Fecha de pago", dto.fechaPago().toString() },
                    { "Total", "$" + dto.montoModificado() },
                    { "Concepto de pago", "Colegiatura semanal # " + dto.numeroSemana().toString() },
                    { "Observaciones", observacionesPagoModificado(dto.monto(), dto.montoModificado()) },
                    { "Semana actual ", "#" + dto.semanaActual().toString() +", PAGO NORMAL: $"+dto.monto().toString()}
            };
            int count = 1;
            for (String[] row : rows) {
                drawRoundedRect(content, tableX, tableY - rowHeight, tableWidth, rowHeight, radius);
                content.stroke();

                // --- LABEL (Left column) ---
                content.beginText();
                content.setFont(ubuntuBold, 12);
                content.newLineAtOffset(tableX + 8, tableY - rowHeight + 7);
                content.showText(row[0] + ":");
                content.endText();

                // --- VALUE (Right column) ---
                content.beginText();
                content.setFont(ubuntu, 12);
                if (count < 4) {
                    count++;
                    content.newLineAtOffset(tableX + 60, tableY - rowHeight + 7);
                } else
                    content.newLineAtOffset(tableX + 125, tableY - rowHeight + 7);
                content.showText(row[1]);
                content.endText();

                // --- VALUE IN LETTERS (only for money rows) ---
                if (row[1].startsWith("$")) {
                    content.beginText();
                    content.setFont(ubuntu, 8);
                    content.newLineAtOffset(tableX + 155, tableY - rowHeight + 7); // slightly lower
                    content.showText("(" + NumeroALetrasUtil.convertirMontoEnLetras(dto.monto()) + ")");
                    content.endText();
                }

                tableY -= rowHeight + 4;
            }

            float boxWidth = 100;
            float boxHeight = 100;
            float boxX = pageWidth - boxWidth - margin;
            float boxY = tableY;

            drawRoundedRect(content, boxX - 30, boxY, boxWidth, boxHeight, 10);
            content.stroke();

            content.beginText();
            content.setFont(ubuntu, 12);
            content.newLineAtOffset(boxX - 20, boxY + boxHeight - 22);
            content.showText("Firma y Sello");
            content.endText();

            content.close();

            String baseDir = System.getProperty("user.home") + "/recibos";
            File dir = new File(baseDir);
            if (!dir.exists())
                dir.mkdirs();

            File file = new File(baseDir + "/" + dto.folio() + ".pdf");
            document.save(file);
            document.close();
            return file;

        }catch (IOException e) {
            e.printStackTrace();
            content.close();
            document.close();
            return null;
        }
    }

    public String observacionesPagoModificado(
            BigDecimal montoOriginal,
            BigDecimal montoModificado) {
        if (montoOriginal == null || montoModificado == null) {
            return "Ninguna";
        }
        int comparacion = montoModificado.compareTo(montoOriginal);
        BigDecimal[] resultado = montoModificado.divideAndRemainder(montoOriginal);

        BigDecimal veces = resultado[0]; // 3
        BigDecimal restante = resultado[1]; // 150

        if (comparacion > 0) {// monto modificado es mayor A 200 ES 750
            
            if (restante.compareTo(BigDecimal.ZERO) == 0) {
                return "Se pagó " + veces + " colegiaturas ";
            } else {
                return "Se pagó " + veces + " colegiatura  y se abono $" + restante + " al siguiente pago";
            }
        }else if (comparacion < 0) {
                return "Se abono $" + montoModificado + " a la colegiatura";
        } else {
                return "El pago no tuvo cambios ($" + montoOriginal + ")";
        }
    }
}

    /**
     * Utility class for creating half-letter PageFormat for printing.
     */
    class PageFormatFactory1 {
        public static PageFormat createPageFormat() {
            PrinterJob job = PrinterJob.getPrinterJob();
            PageFormat pf = job.defaultPage();
            Paper paper = pf.getPaper();

            // Half-letter: 5.5 x 8.5 inches = 396 x 612 points
            paper.setSize(396, 612);
            paper.setImageableArea(10, 10, 376, 592); // margins
            pf.setPaper(paper);

            return pf;
        }

    }

    class PageFormatFactory2 {

        public static PageFormat createPageFormat() {
            PrinterJob job = PrinterJob.getPrinterJob();
            PageFormat pf = job.defaultPage();

            Paper paper = new Paper();
            double width = 396; // 5.5 inches × 72
            double height = 612; // 8.5 inches × 72

            // ✔ Set 0.25-inch printable margins
            double margin = 18; // 0.25 inch = 18pt

            paper.setSize(width, height);
            paper.setImageableArea(margin, margin,
                    width - 2 * margin,
                    height - 2 * margin);

            pf.setPaper(paper);
            return pf;
        }
    }

class PageFormatFactory {

    public static PageFormat createBorderlessPage() {
        PrinterJob job = PrinterJob.getPrinterJob();
        PageFormat pf = job.defaultPage();

        Paper paper = new Paper();
        double width = 612; // 8.5in × 72
        double height = 792; // 11in × 72

        paper.setSize(width, height);

        // ✔ FULL BORDERLESS AREA
        paper.setImageableArea(0, 0, width, height);

        pf.setPaper(paper);

        return pf;
    }
}
