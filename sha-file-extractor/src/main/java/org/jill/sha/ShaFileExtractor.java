/**
 * Extract
 */
package org.jill.sha;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.jill.file.FileAbstractByte;
import org.jill.file.FileAbstractByteImpl;

/**
 * Extractor tool
 *
 * @author Emeric MARTINEAU
 */
public class ShaFileExtractor {
    /**
     * automatically generate the help statement
     */
    private static final HelpFormatter formatter = new HelpFormatter() ;

    /**
     * create Options object
     */
    private static final Options options = new Options() ;

    /**
     * File format to export
     */
    private static final String FILE_EXPORT_FORMAT = "png" ;

    static
    {
        createOptions() ;
    }

    public static void main(String[] args) throws ParseException
    {
        // http://commons.apache.org/cli/usage.html
        CommandLineParser parser = new PosixParser() ;
        CommandLine cmd = parser.parse(options, args) ;

        ExtractParameter param = createParameter(cmd) ;

        // Commands
        if (cmd.hasOption('h'))
        {
            // automatically generate the help statement
            formatter.printHelp(ShaFileExtractor.class.getCanonicalName(),
                    options) ;
        }
        else if(cmd.hasOption('d'))
        {
            showDump(param) ;
        }
        else
        {
            extract(param) ;
        }

    }

    /**
     * Create options of command line
     */
    private static void createOptions()
    {
        // add t option
        options.addOption("d", "dump", false, "dump information from file (header...)") ;
        options.addOption("c", "cga", false, "extract only picture who can display in CGA mode") ;
        options.addOption("e", "ega", false, "extract only picture who can display in EGA mode") ;
        options.addOption("v", "cga", false, "extract only picture who can display in VGA mode") ;
        options.addOption("f", "file", true, "file to read") ;
        options.addOption("o", "out", true, "output directory") ;
        options.addOption("t", "fontonly", false, "extract only font") ;
        options.addOption("p", "pictureonly", false, "extract only picture") ;
        options.addOption("h", "help", false, "print this message") ;
    }

    /**
     * Create parameter
     *
     * @param cmd commande ligne
     *
     * @return parameter object
     */
    private static ExtractParameter createParameter(final CommandLine cmd)
    {
        final ExtractParameter param = new ExtractParameter() ;

        // Parameter
        if (cmd.hasOption('f'))
        {
            param.setFileName(cmd.getOptionValue('f')) ;
        }

        if(cmd.hasOption('o'))
        {
            param.setDirName(cmd.getOptionValue('o')) ;
        }

        if(cmd.hasOption('c'))
        {
            param.setCgaMode(true) ;
        }

        // If one of parameter set, other disable if not set
        if (cmd.hasOption('p') || cmd.hasOption('t'))
        {
            param.setPicture(false) ;
            param.setFont(false) ;
        }

        if(cmd.hasOption('p'))
        {
            param.setPicture(true) ;
        }

        if(cmd.hasOption('t'))
        {
            param.setFont(true) ;
        }

        // If one of parameter set, other disable if not set
        if (cmd.hasOption('c') || cmd.hasOption('e') || cmd.hasOption('v'))
        {
            param.setCgaMode(cmd.hasOption('c')) ;
            param.setEgaMode(cmd.hasOption('e')) ;
            param.setVgaMode(cmd.hasOption('v')) ;
        }

        return param ;
    }

    /**
     * Display dump of file
     *
     * @param param parameter
     */
    private static void showDump(final ExtractParameter param)
    {
        if (param.getFileName() == null)
        {
            System.err.println("Filename of SHA file missing") ;
        }
        else
        {
            try {
                showDumpPrint(param.getFileName()) ;
            } catch (IOException e) {
                e.printStackTrace() ;
            }
        }
    }

    /**
     * Print data
     *
     * @param fileName
     * @throws IOException
     */
    private static void showDumpPrint(final String fileName) throws IOException
    {
        final FileAbstractByte f = new FileAbstractByteImpl();
        f.load(fileName);

        final ShaFile shaFile = new ShaFileImpl();
        shaFile.load(f);

        final ShaHeader shaHeader = shaFile.getShaHeader() ;
        final int[] offset = shaHeader.getOffsetTileSet() ;
        final int[] size = shaHeader.getSizeTileSet() ;

        System.out.println("Entry :") ;
        System.out.println("+-----------------+---------------+----------+") ;
        System.out.println("| Tileset offset  | Tileset size  | is valid |") ;
        System.out.println("+-----------------+---------------+----------+") ;

        for(int index = 0; index < ShaHeaderImpl.ENTRY_NUMBER; index++)
        {
            System.out.println(
                String.format("|     %08X    |      %04X     | %8b |",
                    new Object[] {
                        Integer.valueOf(offset[index]),
                        Integer.valueOf(size[index]),
                        Boolean.valueOf(shaHeader.isValideEntry(index))
                        })
                        ) ;
            System.out.println("+-----------------+---------------+----------+") ;
        }

        System.out.println("") ;

        System.out.println("Tileset :") ;
        System.out.println("+-------+----------+------+----------+----------+----------+----------+----------+-------+---------+") ;
        System.out.println("| Index |  Offset  | Size | Nb pict  | Cga size | Ega size | Vga size | bt color | Font  | Picutre |") ;
        System.out.println("+-------+----------+------+----------+----------+----------+----------+----------+-------+---------+") ;


        final ShaTileSet[] shaTileset = shaFile.getShaTileSet() ;
        ShaTileSet currentTileset ;

        for(int index = 0; index < shaTileset.length; index++)
        {
            currentTileset = shaTileset[index] ;

            System.out.println(
                String.format("|  %3d  | %08X | %04X |  %5d   |  %5d   |  %5d   |   %5d  |    %3d   | %5b |  %5b  |",
                    new Object[] {
                        Integer.valueOf(currentTileset.getTitleSetIndex()),
                        Integer.valueOf(currentTileset.getOffset()),
                        Integer.valueOf(currentTileset.getSize()),
                        Integer.valueOf(currentTileset.getNumberTile()),
                        Integer.valueOf(currentTileset.getCgaSize()),
                        Integer.valueOf(currentTileset.getEgaSize()),
                        Integer.valueOf(currentTileset.getVgaSize()),
                        Integer.valueOf(currentTileset.getBitColor()),
                        Boolean.valueOf(currentTileset.isFont()),
                        Boolean.valueOf(currentTileset.isTileset())
                        })
                );

            System.out.println("+-------+----------+------+----------+----------+----------+----------+----------+-------+---------+") ;
        }

        System.out.println("Cga/Ega/Vga size is size in video memory after decompress") ;
        System.out.println("Nb color is bit depth of colour map");


        System.out.println("") ;
        System.out.println("+----------+---------+-------+-------+-------+-------------+") ;
        System.out.println("|  Offset  | Tileset | Tile  | Widht | Height| Data format |") ;
        System.out.println("+----------+---------+-------+-------+-------+-------------+") ;

        int indexTile ;
        ShaTile[] tileArray ;
        ShaTile tile ;

        for(int index = 0; index < shaTileset.length; index++)
        {
            currentTileset = shaTileset[index] ;
            tileArray = currentTileset.getShaTile() ;

            for(indexTile = 0; indexTile < tileArray.length; indexTile++)
            {
                tile = tileArray[indexTile] ;

                System.out.println(
                        String.format("| %08X |   %3d   |  %3d  |  %3d  |  %3d  |     %3d     |",
                            new Object[] {
                                Integer.valueOf(tile.getOffset()),
                                Integer.valueOf(tile.getTitleSetIndex()),
                                Integer.valueOf(tile.getImageIndex()),
                                Integer.valueOf(tile.getWidth()),
                                Integer.valueOf(tile.getHeight()),
                                Integer.valueOf(tile.getDataFormat()),
                                })
                        );

            }
        }

        System.out.println("+----------+---------+-------+-------+-------+-------------+") ;
    }

    /**
     * Ectract font or picture
     *
     * @param param parameter
     */
    private static void extract(final ExtractParameter param)
    {
        if (param.getDirName() == null || "".equals(param.getDirName().trim()))
        {
            param.setDirName(".") ;
        }

        if (param.getFileName() == null)
        {
            System.err.println("Filename of SHA file missing") ;
        }
        else
        {
            try {
                extractCore(param) ;
            } catch (IOException e) {
                e.printStackTrace() ;
            }
        }
    }

    /**
     * Extract font only
     *
     * @param fileName file to read
     * @param dirName where to write
     * @throws IOException
     */
    private static void extractCore(final ExtractParameter param) throws IOException
    {
        final FileAbstractByte f = new FileAbstractByteImpl();
        f.load(param.getFileName());
        
        final ShaFile shaFile = new ShaFileImpl();
        shaFile.load(f);

        final ShaTileSet[] shaTileset = shaFile.getShaTileSet() ;
        final String tileFileNamePattern = param.getDirName().concat("/tileset_%d_tile_%d_%s.png") ;
        final boolean picture = param.isPicture() ;
        final boolean font = param.isFont() ;
        final boolean cgaMode = param.isCgaMode() ;
        final boolean egaMode = param.isEgaMode() ;
        final boolean vgaMode = param.isVgaMode() ;

        ShaTileSet currentTileset ;
        ShaTile[] shaTile ;

        String tileFileName ;
        int numberfontExtract = 0 ;
        boolean isFont ;

//        FileWriter outFile = null ;
//        PrintWriter out = null ;
//        outFile = new FileWriter("w:/liste_fichiers.txt");
//        out = new PrintWriter(outFile) ;
//
//        String titleSetDisplay ;

        for(int indexTitleSet = 0; indexTitleSet < shaTileset.length; indexTitleSet++)
        {
            currentTileset = shaTileset[indexTitleSet] ;

            isFont = currentTileset.isFont() ;

            shaTile = currentTileset.getShaTile() ;

//            titleSetDisplay = String.valueOf(currentTileset.getTitleSetIndex()) ;

            // Font only
            if (font && isFont)
            {
                for(int indexTile = 0; indexTile < shaTile.length; indexTile++)
                {
                    tileFileName = String.format(tileFileNamePattern,
                            new Object[] {
                                Integer.valueOf(currentTileset.getTitleSetIndex()),
                                Integer.valueOf(indexTile),
                                "font"})  ;

                    writeFile(shaTile[indexTile].getPictureVga(), tileFileName) ;

                    numberfontExtract++ ;
//                    out.println("| "+titleSetDisplay+" | "+String.valueOf(indexTile)+" | FONT | {{http://ns10.firstheberg.com/~bubule/web/download/jill/tile/" + tileFileName + "}} |");
//                    titleSetDisplay = ":::" ;
                }
            }

            // Picture only
            if (picture && !isFont)
            {
                for(int indexTile = 0; indexTile < shaTile.length; indexTile++)
                {
//                    out.print("| "+titleSetDisplay+" | "+String.valueOf(indexTile)+" | PICTURE | ");
//                    titleSetDisplay = ":::" ;

                    if (cgaMode && (currentTileset.getBitColor() != 8))
                    {
                        tileFileName = String.format(tileFileNamePattern,
                            new Object[] {
                                Integer.valueOf(currentTileset.getTitleSetIndex()),
                                Integer.valueOf(indexTile),
                                "cga"})  ;

                        writeFile(shaTile[indexTile].getPictureCga(), tileFileName) ;

                        numberfontExtract++ ;

//                        out.print(" {{http://ns10.firstheberg.com/~bubule/web/download/jill/tile/" + tileFileName + "}} \\\\ ");
                    }

                    if (egaMode && (currentTileset.getBitColor() != 8))
                    {
                        tileFileName = String.format(tileFileNamePattern,
                            new Object[] {
                                Integer.valueOf(currentTileset.getTitleSetIndex()),
                                Integer.valueOf(indexTile),
                                "ega"})  ;

                        writeFile(shaTile[indexTile].getPictureEga(), tileFileName) ;

                        numberfontExtract++ ;

//                        out.print(" {{http://ns10.firstheberg.com/~bubule/web/download/jill/tile/" + tileFileName + "}} \\\\ ");
                    }

                    if (vgaMode)
                    {
                        tileFileName = String.format(tileFileNamePattern,
                            new Object[] {
                                Integer.valueOf(currentTileset.getTitleSetIndex()),
                                Integer.valueOf(indexTile),
                                "vga"})  ;

                        writeFile(shaTile[indexTile].getPictureVga(), tileFileName) ;

                        numberfontExtract++ ;

//                        out.print(" {{http://ns10.firstheberg.com/~bubule/web/download/jill/tile/" + tileFileName + "}} \\\\ ");
                    }

//                    out.println("") ;
//
//                    titleSetDisplay = ":::" ;
                }
            }
        }

//        out.close();

        System.out.println(String.format("Item extracted : %d",
                new Object[] {Integer.valueOf(numberfontExtract) }
                ));
    }

    /**
     * Write file to disk
     *
     * @param bi picture
     * @param tileFileName filename
     *
     * @throws IOException
     */
    private static void writeFile(final BufferedImage bi, final String tileFileName) throws IOException
    {
        File outputfile ;

        System.out.println(tileFileName) ;

        outputfile = new File(tileFileName) ;
        ImageIO.write(bi, FILE_EXPORT_FORMAT, outputfile) ;
    }
}