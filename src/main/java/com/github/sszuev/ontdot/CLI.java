package com.github.sszuev.ontdot;

import com.github.owlcs.ontapi.OntFormat;
import com.github.sszuev.ontdot.api.DOTSetting;
import org.apache.commons.cli.*;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by @ssz on 12.01.2022.
 */
class CLI {
    private static final int DEFAULT_WIDTH = (int) (HelpFormatter.DEFAULT_WIDTH * 2.5);
    private static final Set<String> HELP_REQUEST = Set.of("/?", "/help", "-help", "--help", "h", "-h", "--h");

    private final Path source;
    private final OntFormat format;
    private final Path target;
    private final boolean verbose;
    private final boolean browse;
    private final boolean asURL;
    private final List<String> filterEntities;
    private final Map<DOTSetting, Object> optionsToEnable;

    CLI(Path source, OntFormat format,
        Path target, boolean verbose, boolean browse, boolean asURL,
        List<String> filterEntities, Map<DOTSetting, Object> optionsToEnable) {
        this.source = source;
        this.format = format;
        this.target = target;
        this.verbose = verbose;
        this.browse = browse;
        this.asURL = asURL;
        this.filterEntities = filterEntities;
        this.optionsToEnable = optionsToEnable;
    }

    public static CLI parse(String... args) {
        Options options = buildOptions();
        try {
            if (args.length == 0 || Arrays.stream(args).anyMatch(HELP_REQUEST::contains)) {
                throw new ExitException(0, printHelp(options), null);
            }
            CommandLine cmd = new DefaultParser().parse(options, args);
            if (cmd.hasOption("h")) {
                throw new ExitException(args.length == 1 ? 0 : 1, printHelp(options), null);
            }
            Path source = parseSource(cmd);
            OntFormat format = parseFormat(cmd);
            Path target = parseTarget(cmd, source);
            boolean browse = cmd.hasOption("b");
            boolean verbose = cmd.hasOption("v");
            boolean asURL = cmd.hasOption("u");
            List<String> entities = parseFilterEntities(cmd);
            Map<DOTSetting, Object> settings = parseOptions(cmd);
            return new CLI(source, format, target, verbose, browse, asURL, entities, settings);
        } catch (ParseException e) {
            throw new ExitException(2, e.getMessage(), e);
        } catch (IOException e) {
            throw new ExitException(3, String.format("%s: %s", e.getClass().getSimpleName(), e.getMessage()), e);
        }
    }

    private static Path parseSource(CommandLine cmd) throws IOException {
        return Paths.get(cmd.getOptionValue("i")).toRealPath();
    }

    private static OntFormat parseFormat(CommandLine cmd) throws ParseException {
        if (!cmd.hasOption("if")) {
            return null;
        }
        return get(cmd.getOptionValue("if"));
    }

    private static Path parseTarget(CommandLine cmd, Path source) throws IOException {
        if (!cmd.hasOption("o")) {
            return null;
        }
        String target = cmd.getOptionValue("o");
        Path res = Paths.get(target);
        return res.isAbsolute() ? res : source.getParent().resolve(res).toAbsolutePath();
    }

    private static List<String> parseFilterEntities(CommandLine cmd) {
        if (!cmd.hasOption("e")) {
            return List.of();
        }
        return Arrays.asList(cmd.getOptionValue("e").split(",\\s*"));
    }

    private static Map<DOTSetting, Object> parseOptions(CommandLine cmd) throws ParseException {
        if (!cmd.hasOption("B")) {
            return Map.of();
        }
        Map<DOTSetting, Object> res = new EnumMap<>(DOTSetting.class);
        String[] opts = cmd.getOptionValues("B");
        if (opts.length % 2 != 0) {
            throw new ParseException("Wrong options :" + Arrays.toString(opts));
        }
        for (int i = 0; i < opts.length; i += 2) {
            DOTSetting key = DOTSetting.ofKey(opts[i]);
            if (key == null) {
                throw new ParseException("Unknown option: " + opts[i]);
            }
            String value = opts[i + 1];
            if (key.type() == Boolean.class) {
                res.put(key, Boolean.parseBoolean(value));
            } else if (key.type() == Integer.class) {
                res.put(key, Integer.parseInt(value));
            } else {
                res.put(key, value);
            }
        }
        return res;
    }

    private static OntFormat get(String key) throws ParseException {
        return OntFormat.formats().filter(f -> aliases(f).anyMatch(x -> x.equalsIgnoreCase(key))).findFirst()
                .orElseThrow(() -> new ParseException("Unsupported format: '" + key + "'"));
    }

    private static Stream<String> aliases(OntFormat f) {
        return Stream.of(String.valueOf(f.ordinal()), f.name(), f.getID(), f.getExt())
                .map(String::toLowerCase)
                .distinct();
    }

    private static Options buildOptions() {
        return new Options()
                .addOption(buildHelpOption())
                .addOption(Option.builder("i")
                        .longOpt("input")
                        .desc("The source OWL RDF-ontology file path (e.g. ttl)")
                        .hasArg()
                        .required(true)
                        .build())
                .addOption(Option.builder("if")
                        .longOpt("input-format")
                        .desc("The input format. Optional: " +
                                "if not specified the program will choose the most suitable one " +
                                "to load ontology from a file. " +
                                "Must be one of the following: " + availableFormats())
                        .required(false)
                        .hasArg()
                        .build())
                .addOption(Option.builder("v")
                        .longOpt("verbose")
                        .desc("To print progress messages and logs to console.")
                        .required(false)
                        .build())
                .addOptionGroup(buildOutputOption())
                .addOption(Option.builder("u")
                        .longOpt("url")
                        .desc("Print as url (suitable for small documents).")
                        .required(false)
                        .build())
                .addOption(Option.builder("e")
                        .longOpt("entities")
                        .desc("A list of entities to filter, " +
                                "full or short (prefixed) IRIs, dot as separator, " +
                                "example for pizza-ontology: '-e :Veneziana,:Germany'")
                        .required(false)
                        .hasArg(true)
                        .build())
                .addOption(Option.builder("B")
                        .numberOfArgs(DOTSetting.values().length).valueSeparator('=')
                        .desc("Options to control rendering, format is -Bkey=value," +
                                "e.g. '-BclassPropertiesMap=true' will turn on displaying class-properties map.\n" +
                                "Available options:\n" + availableSettings())
                        .required(false)
                        .build())
                ;
    }

    private static OptionGroup buildOutputOption() {
        OptionGroup res = new OptionGroup();
        res.setRequired(false);
        res.addOption(Option.builder("o")
                .longOpt("output")
                .desc("The output file to write dot-content. " +
                        "Optional: if not specified the program will print result to stdout.")
                .hasArg()
                .required(false)
                .build());
        res.addOption(Option.builder("b")
                .longOpt("browse")
                .desc("Tells the program to open the result graph in the default browser.")
                .required(false)
                .build());
        res.addOption(buildHelpOption());
        return res;
    }

    private static Option buildHelpOption() {
        return Option.builder("h")
                .longOpt("help")
                .desc("Display this usage")
                .build();
    }

    private static String printHelp(Options options) {
        HelpFormatter hf = new HelpFormatter();
        hf.setOptionComparator(null);
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        hf.printHelp(pw, DEFAULT_WIDTH, cmdLineSyntax(), null, options, hf.getLeftPadding(), hf.getDescPadding(), null);
        pw.flush();
        return sw.toString();
    }

    private static String cmdLineSyntax() {
        return "-i <path-to-input-rdf-file> [-if <format>] " +
                "[-o <output-file-dot>]|[-b][-v][-u] [-e filterEntities][-Boption-key=option-value]";
    }

    private static String availableFormats() {
        return OntFormat.formats().filter(OntFormat::isReadSupported).map(Enum::name).collect(Collectors.joining(", "));
    }

    private static String availableSettings() {
        return Arrays.stream(DOTSetting.values()).map(CLI::printSetting).collect(Collectors.joining("\n"));
    }

    private static String printSetting(DOTSetting setting) {
        return String.format("%s%s%s",
                StringUtils.rightPad("-B" + setting.key(), 36),
                StringUtils.rightPad("(" + setting.type().getSimpleName().toLowerCase(Locale.ENGLISH) + ")", 9),
                " -- " + setting.description());
    }

    public Path source() {
        return source;
    }

    public OntFormat format() {
        return format;
    }

    public Path target() {
        return target;
    }

    public boolean verbose() {
        return verbose;
    }

    public boolean browse() {
        return browse;
    }

    public boolean printAsURL() {
        return asURL;
    }

    public List<String> filterEntities() {
        return filterEntities;
    }

    public Map<DOTSetting, Object> options() {
        return optionsToEnable;
    }

    static class ExitException extends RuntimeException {
        private final int code;

        public ExitException(int code, String message, Throwable cause) {
            super(message, cause);
            this.code = code;
        }

        public int getCode() {
            return code;
        }
    }
}
