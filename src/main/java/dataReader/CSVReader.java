package dataReader;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Suavek on 08/11/2016.
 */
public class CSVReader {

    private final CSVParser csvFileParser; // this is object of type iterable
    private final List<CSVRecord> csvRecordList;

    public CSVReader(String filePath) throws IOException {
        // Get and read File
        final File file = new File(filePath);
        final Reader fileReader = new FileReader(file);
        // Parse file
        final CSVFormat csvFileFormat = CSVFormat.DEFAULT.withFirstRecordAsHeader();
        this.csvFileParser = new CSVParser(fileReader, csvFileFormat);
        this.csvRecordList = csvFileParser.getRecords();
    }

    public ArrayList getAttributeNames() {
        return new ArrayList(csvFileParser.getHeaderMap().keySet());
    }

    public List<CSVRecord> getDataSet() {
        return this.csvRecordList;
    }
}
