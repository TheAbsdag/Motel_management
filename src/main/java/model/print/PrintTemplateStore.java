package model.print;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.json.ObjectMapperFactory;

/**
 * Loads and persists print templates.
 *
 * <p>Custom templates live in {@code <base>/data/printTemplates/<type>.json}. A
 * missing file is a valid state: the printer then keeps using its built-in layout,
 * and the editor starts from the packaged factory default instead.
 *
 * <p>Writes are atomic ({@code .tmp} file followed by a move), like the rest of the
 * application's file I/O.
 */
public final class PrintTemplateStore {

    private static final Logger LOGGER = Logger.getLogger(PrintTemplateStore.class.getName());
    private static final String DIRECTORY = "printTemplates";
    private static final String RESOURCE_ROOT = "/printTemplates/";

    private final Path directory;

    /**
     * @param basePath the application base directory (usually {@code FileManager.PATH})
     */
    public PrintTemplateStore(Path basePath) {
        this.directory = basePath.resolve("data").resolve(DIRECTORY);
    }

    /**
     * @param type the template type
     * @return the file holding the custom template of that type
     */
    public Path fileFor(PrintTemplateType type) {
        return directory.resolve(type.fileName());
    }

    /**
     * @param type the template type
     * @return {@code true} when a custom template has been saved
     */
    public boolean exists(PrintTemplateType type) {
        return Files.isRegularFile(fileFor(type));
    }

    /**
     * Reads the custom template of a type.
     *
     * @param type the template type
     * @return the saved template, or {@code null} when it does not exist or cannot be read
     */
    public PrintTemplate load(PrintTemplateType type) {
        Path file = fileFor(type);
        if (!Files.isRegularFile(file)) {
            return null;
        }
        try {
            String json = Files.readString(file, StandardCharsets.UTF_8);
            return ObjectMapperFactory.get().readValue(json, PrintTemplate.class);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "No se pudo leer la plantilla " + file, e);
            return null;
        }
    }

    /**
     * Reads the template to edit: the custom one when it exists, the packaged default otherwise.
     *
     * @param type the template type
     * @return the template to show in the editor, or {@code null} when neither is available
     */
    public PrintTemplate loadOrDefault(PrintTemplateType type) {
        PrintTemplate custom = load(type);
        return custom != null ? custom : defaultTemplate(type);
    }

    /**
     * Writes the custom template of a type.
     *
     * @param type     the template type
     * @param template the template to persist
     * @return {@code true} when the template was written
     */
    public boolean save(PrintTemplateType type, PrintTemplate template) {
        Path target = fileFor(type);
        Path tmp = target.resolveSibling(target.getFileName() + ".tmp");
        try {
            Files.createDirectories(directory);
            ObjectMapper mapper = ObjectMapperFactory.get();
            Files.writeString(tmp, mapper.writeValueAsString(template), StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "No se pudo escribir la plantilla " + tmp, e);
            return false;
        }
        try {
            Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Movimiento atómico falló, reintentando", e);
            try {
                Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException fallback) {
                LOGGER.log(Level.SEVERE, "No se pudo guardar la plantilla " + target, fallback);
                return false;
            }
        }
        return true;
    }

    /**
     * Removes the custom template so the built-in layout is used again.
     *
     * @param type the template type
     * @return {@code true} when a custom template was deleted
     */
    public boolean delete(PrintTemplateType type) {
        try {
            return Files.deleteIfExists(fileFor(type));
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "No se pudo borrar la plantilla " + fileFor(type), e);
            return false;
        }
    }

    /**
     * Reads the factory default packaged with the application.
     *
     * @param type the template type
     * @return the packaged template, or {@code null} when the resource is missing
     */
    public PrintTemplate defaultTemplate(PrintTemplateType type) {
        String resource = RESOURCE_ROOT + type.fileName();
        try (InputStream stream = PrintTemplateStore.class.getResourceAsStream(resource)) {
            if (stream == null) {
                LOGGER.log(Level.WARNING, "No se encontró la plantilla por defecto " + resource);
                return null;
            }
            return ObjectMapperFactory.get().readValue(stream, PrintTemplate.class);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "No se pudo leer la plantilla por defecto " + resource, e);
            return null;
        }
    }
}
