/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2022 Objectionary
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.eolang.speco;

import com.jcabi.xml.XML;
import com.jcabi.xml.XMLDocument;
import com.yegor256.xsline.Shift;
import com.yegor256.xsline.StClasspath;
import com.yegor256.xsline.StEndless;
import com.yegor256.xsline.TrDefault;
import com.yegor256.xsline.Train;
import com.yegor256.xsline.Xsline;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.commons.io.FileUtils;
import org.cactoos.io.InputOf;
import org.cactoos.io.OutputTo;
import org.eolang.parser.Syntax;
import org.eolang.parser.XMIR;
import org.objectionary.aoi.launch.LauncherKt;

/**
 * The class encapsulating specialization logic.
 *
 * @since 0.0.1
 */
final class Speco {

    /**
     * Absolute path to the directory with input files.
     */
    private final Path input;

    /**
     * Absolute path to the directory with output files.
     */
    private final Path output;

    /**
     * Flag indicating whether the input files is EO-program.
     */
    private final boolean eolang;

    /**
     * Ctor.
     *
     * @param inp Path to the directory with input files
     * @param out Path to the directory with output files
     * @param eolang Iff the input program is in EO
     */
    Speco(final Path inp, final Path out, final boolean eolang) {
        this.input = inp.toAbsolutePath();
        this.output = out.toAbsolutePath();
        this.eolang = eolang;
    }

    /**
     * Starts the specialization process.
     *
     * @throws IOException In case of errors when working with files or parsing a document
     */
    void exec() throws IOException {
        final Path source;
        if (this.eolang) {
            source = Speco.parse(this.input);
        } else {
            source = this.input;
        }
        for (final Path path : Files.newDirectoryStream(source)) {
            final String transformed = Speco.applyTrain(
                new Xsline(
                    new TrDefault<Shift>().with(
                        new StClasspath("/org/eolang/parser/wrap-method-calls.xsl")
                    )
                ).pass(new XMLDocument(Files.readString(path)))
            ).toString();
            final String after;
            if (this.eolang) {
                after = new XMIR(transformed).toEO();
            } else {
                after = transformed;
            }
            Files.createDirectories(this.output);
            Files.write(this.output.resolve(path.getFileName()), after.getBytes());
        }
    }

    /**
     * Applies train of XSL-transformations.
     *
     * @param xml XML
     * @return XML
     */
    private static XML applyTrain(final XML xml) {
        final Train<Shift> train = new TrDefault<Shift>()
            .with(new StClasspath("/org/eolang/speco/1-1-coping.xsl"))
            .with(new StEndless(new StClasspath("/org/eolang/speco/1-2-specialization.xsl")))
            .with(new StClasspath("/org/eolang/speco/1-3-extension.xsl"))
            .with(new StClasspath("/org/eolang/speco/2-1-substitute-applications.xsl"))
            .with(new StClasspath("/org/eolang/speco/3-1-add-with.xsl"))
            .with(new StClasspath("/org/eolang/speco/4-1-fence-tuples.xsl"))
            .with(new StClasspath("/org/eolang/speco/5-1-substitute-fence.xsl"))
            .with(new StClasspath("/org/eolang/speco/6-1-substitute-dominant.xsl"))
            .with(new StClasspath("/org/eolang/speco/7-1-substitute-returned.xsl"));
        return new Xsline(train).pass(xml);
    }

    /**
     * Takes source codes on EO, converts to xmir and applies the AOI tool.
     *
     * @param input Path of the source directory
     * @return Path to the directory with the parsed files
     * @throws IOException When Parsing EO fails
     */
    private static Path parse(final Path input) throws IOException {
        final StringBuilder name = new StringBuilder(input.toString());
        final Path source = Path.of(name.append("_prs").toString());
        FileUtils.copyDirectory(input.toFile(), source.toFile());
        for (final Path path : Files.newDirectoryStream(source)) {
            new Syntax(
                "scenario",
                new InputOf(String.format("%s%n", Files.readString(path))),
                new OutputTo(new FileOutputStream(path.toFile()))
            ).parse();
        }
        LauncherKt.launch(source.toString());
        return Path.of(name.append("_aoi").toString());
    }
}
