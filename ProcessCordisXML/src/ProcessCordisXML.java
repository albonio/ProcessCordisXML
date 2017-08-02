import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.tika.langdetect.OptimaizeLangDetector;
import org.apache.tika.language.detect.LanguageDetector;
import org.apache.tika.language.detect.LanguageResult;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;


public class ProcessCordisXML {
	static String outputDir = "output";
    static String inputDir  = "input"; 
    
    private static boolean eu_us_comparative = false;
    
    private static int maxProjectDurationYears = 4; // TODO depend on frameProgram???
    private static int minProjecTotalCost = 22222; // TODO depend on frameProgram???
    private static int METHOD_MEAN = 1;
    private static int METHOD_MIN = 2;
    private static int METHOD_STATIC_MIN = 3;
    private static int methodProjecTotalCost_FP1 = METHOD_STATIC_MIN;
    private static int methodProjecTotalCost_FP2 = METHOD_MEAN;
    private static int methodProjecTotalCost_FP3 = METHOD_MEAN;
    private static int methodProjecTotalCost_FP4 = METHOD_MEAN;
    private static int methodProjecTotalCost_FP5 = METHOD_MEAN;
    private static int methodProjecTotalCost_FP6 = METHOD_MEAN;
    private static int methodProjecTotalCost_FP7 = METHOD_MEAN;
    private static int methodProjecTotalCost_H2020 = METHOD_MEAN;


    // short project remove
    private static boolean removeShortTextProjects = true;
    private static int shortTextProjectsThreshold = 100;
    private static List<Integer> shortTextProjectsList = new ArrayList<>();
    
    // non eng project remove 
    private static boolean removeOtherLangProjects = true;
    private static String mainLang = "en"; //TODO List
    private static List<Integer> removeOtherLangProjectsList = new ArrayList<>();
    private static LanguageDetector langDetector =  null;
    		 
    private static String H2020projectsZipfile 	= "cordis-H2020projects-xml.zip";
	private static String H2020projectsFile		= "cordis-H2020projects";
	private static String FP7projectsZipfile 	= "cordis-fp7projects-xml.zip";
	private static String FP7projectsFile 		= "cordis-FP7projects";
	private static String FP6projectsZipfile 	= "cordis-fp6projects-xml.zip";
	private static String FP6projectsFile 		= "cordis-FP6projects";	
	private static String FP5projectsZipfile 	= "cordis-fp5projects-xml.zip";
	private static String FP5projectsFile 		= "cordis-FP5projects";	
	
	private static String FP4projectsCsvfile 	= "cordisfp4complete.csv";
	private static String FP4projectsFile 		= "cordis-FP4projects";		
	
	private static String FP3projectsZipfile 	= "cordis-fp3projects-xml.zip";
	private static String FP3projectsFile 		= "cordis-FP3projects";	
	private static String FP2projectsZipfile 	= "cordis-fp2projects-xml.zip";
	private static String FP2projectsFile 		= "cordis-FP2projects";	
	private static String FP1projectsZipfile 	= "cordis-fp1projects-xml.zip";
	private static String FP1projectsFile 		= "cordis-FP1projects";	
	
	private static String projectsFile 			= "cordis-projects";
	private static boolean splitFiles = false;//true;

	private static boolean FP1process 	= true;
	private static boolean FP2process 	= true;
	private static boolean FP3process	= true;
	private static boolean FP4process 	= true;
	private static boolean FP5process 	= true;
	private static boolean FP6process 	= true;
	private static boolean FP7process 	= true;
	private static boolean H2020process = true;
	
    private static DecimalFormat df4;
	
	private static int[] ldaTextProjectsFields = {2,3};// Objective + Title
	private static String[] FP4Strings = {"THE EXPERIMENT%L%L", "THE EXPERIMENT %L%L", "ACTIONS%L%L", "ACTIONS%L", "THE DEVELOPMENT%L", "INNOVATIVE TECHNOLOGY :%L", "INNOVATIVE TECHNOLOGY: ", "INNOVATIVE TECHNOLOGY : ", "Research objectives and content%L%L", "INTRODUCTION AND RATIONALE%L%L", "INTRODUCTION%L%L", "Background :%L%L", "Background%L%L", "Background%L", "Background %L%L", "Technical Approach%L", "Objective: %L", "OBJECTIVES: %L", "THE EXPERIMENT%L", "EXPECTED IMPACT AND EXPERIENCE%L%L", "METHODOLOGY%L%L", "DESCRIPTION OF WORK%L%L", "METHODOLOGY AND RESEARCH TASKS%L%L", "DESCRIPTION OF THE WORK%L%L", "WORK AND ACHIEVEMENTS%L%L", "INTRODUCTION%L", "RATIONALE%L%L", "Objectives %L%L", "INNOVATION%L", "Objectives %L", "%LPROJECT", "INNOVATIVE TECHNOLOGY%L", "THE CONTEXT IN WHICH THE TECHNOLOGY IS OPERATING%L", "THE NEW BLADE ANGLE VARIATION SYSTEM%L", "%L%LTECHNICAL APPROACH %L%L", "%LEXPECTED ACHIEVEMENTS AND EXPLOITATION %L%L", "%L%LEXPECTED IMPACT AND EXPERIENCE%L", "%L%LEXPECTED IMPACT%L%L", "%L%LEXPECTED IMPACT AND EXPERIENCE %L%L", "TECHNICAL SUMMARY %L", "Objectives%L", "%L %L OBJECTIVES.%L %L", "%L %L METHODOLOGY.%L %L", "%L %L Method: %L", "%LFIELD TRIALS%L", "%L%LCase studies%L%L", "ENGINEERING WORKS%L", "THE DERIVATION%L", "SUMMARY%L", "%LMain deliverables%L", "Technical Approach %L", "%LUsers %L", "Contribution to EU Policies%L", "Key issues%L", "Workpackage objectives:%L", "%LLABORATORY TEST PROGRAMMME%L", "%LANALYTICAL WORK%L", "MANUFACTURE%L", "INSTALLATION%L", "SEA TEST%L", "RESEARCH TASKS%L", "%L %L", "%L%L", "%L", "The key activities envisaged are:", "Objectives and content", "Objectives and content: ", "Objectives : ", "Objectives: ", "The key activities involve[ ]*:[ ]*"};
	//eu_us_comparative
	private static int[] metadataProjectsFields = {3,4,6,7};//{0,3,4,5,6,7,8,9};
	private static String[] metadataProjectsHeaders = {"Acronym","Teaser","Objective","Title","TotalCost","EcMaxContribution","StartDate","EndDate","ContractDuration","Status"};
	private static int totalCostPosition = 4;
	
	// frameProgram
	private static final int FP1 = 1;
	private static final int FP2 = 2;
	private static final int FP3 = 3;
	private static final int FP4 = 4;
	private static final int FP5 = 5;
	private static final int FP6 = 6;
	private static final int FP7 = 7;
	private static final int H2020 = 20;
	
	
    // create a new DocumentBuilderFactory
    static DocumentBuilderFactory factory;
    // use the factory to create a documentbuilder
    static DocumentBuilder builder;
    // Create XPathFactory object
    static XPathFactory xpathFactory;
    // Create XPath object
    static XPath xpath;    
    
    // XPath expressions
    static XPathExpression exprProjectName;
    static XPathExpression exprProjectLanguage;
    static XPathExpression exprProjectAcronym;
    static XPathExpression exprProjectTeaser;   
    static XPathExpression exprProjectObjective;
    static XPathExpression exprProjectTitle;
    static XPathExpression exprProjectTotalCost;
    static XPathExpression exprProjectEcMaxContribution;
    static XPathExpression exprProjectStartDate;
    static XPathExpression exprProjectEndDate;
    static XPathExpression exprProjectContractDuration;
    static XPathExpression exprProjectStatus;

    private static final char DEFAULT_SEPARATOR = ';';
    

    
	public static void main(String[] args) throws Exception {
        DecimalFormatSymbols simbolos = new DecimalFormatSymbols();
        simbolos.setDecimalSeparator('.');    
        df4 = new DecimalFormat("#", simbolos);
        
		// delete output folder
        deleteFolder(new File(outputDir), false);
        File outputDirFile = new File(outputDir);
        outputDirFile.mkdir();	

        // xml init
        factory = DocumentBuilderFactory.newInstance();
        builder = factory.newDocumentBuilder();
        xpathFactory = XPathFactory.newInstance();
        xpath = xpathFactory.newXPath();    
        
        // xpath init
        initXPathExpressions();
        
        // read XML contents
        HashMap<Integer, List<String>> xmlContentsFP1 	= null;
        HashMap<Integer, List<String>> xmlContentsFP2 	= null;
        HashMap<Integer, List<String>> xmlContentsFP3 	= null;
        HashMap<Integer, List<String>> xmlContentsFP4 	= null;
        HashMap<Integer, List<String>> xmlContentsFP5 	= null;
        HashMap<Integer, List<String>> xmlContentsFP6 	= null;
        HashMap<Integer, List<String>> xmlContentsFP7 	= null;
        HashMap<Integer, List<String>> xmlContentsH2020 	= null;
        		
        // readc Zip file
        if(FP1process){
        	System.out.println("\nProcessing FP1 projects:");
        	xmlContentsFP1 	= readCordisZipFile(inputDir  + File.separator + FP1projectsZipfile, FP1);
        	if(methodProjecTotalCost_FP1 == METHOD_MIN){
        		calculateMinProjecTotalCost(xmlContentsFP1);
        	} else if(methodProjecTotalCost_FP1 == METHOD_MEAN){
        		calculateMeanProjecTotalCost(xmlContentsFP1);
        	}
        } 
        if(FP2process){
        	System.out.println("\nProcessing FP2 projects:");
        	xmlContentsFP2 	= readCordisZipFile(inputDir  + File.separator + FP2projectsZipfile, FP2);
        	if(methodProjecTotalCost_FP2 == METHOD_MIN){
        		calculateMinProjecTotalCost(xmlContentsFP2);
        	} else if(methodProjecTotalCost_FP2 == METHOD_MEAN){
        		calculateMeanProjecTotalCost(xmlContentsFP2);
        	}
        } 
        if(FP3process){
        	System.out.println("\nProcessing FP3 projects:");
        	xmlContentsFP3 	= readCordisZipFile(inputDir  + File.separator + FP3projectsZipfile, FP3);
        	if(methodProjecTotalCost_FP3 == METHOD_MIN){
        		calculateMinProjecTotalCost(xmlContentsFP3);
        	} else if(methodProjecTotalCost_FP3 == METHOD_MEAN){
        		calculateMeanProjecTotalCost(xmlContentsFP3);
        	}
        } 
        
        if(FP4process){
        	System.out.println("\nProcessing FP4 projects:");
        	HashMap<Integer, List<String>> csvContentsFP4 = CSVUtil.readCSV(inputDir  + File.separator + FP4projectsCsvfile);
        	xmlContentsFP4 	= readCordisCsvFile(csvContentsFP4);
        	if(methodProjecTotalCost_FP4 == METHOD_MIN){
        		calculateMinProjecTotalCost(xmlContentsFP4);
        	} else if(methodProjecTotalCost_FP4 == METHOD_MEAN){
        		calculateMeanProjecTotalCost(xmlContentsFP4);
        	}
        } 
        
        if(FP5process){
        	System.out.println("\nProcessing FP5 projects:");
        	xmlContentsFP5 	= readCordisZipFile(inputDir  + File.separator + FP5projectsZipfile, FP5);
        	if(methodProjecTotalCost_FP5 == METHOD_MIN){
        		calculateMinProjecTotalCost(xmlContentsFP5);
        	} else if(methodProjecTotalCost_FP5 == METHOD_MEAN){
        		calculateMeanProjecTotalCost(xmlContentsFP5);
        	}
        } 
        if(FP6process){
        	System.out.println("\nProcessing FP6 projects:");
        	xmlContentsFP6 	= readCordisZipFile(inputDir  + File.separator + FP6projectsZipfile, FP6);
        	if(methodProjecTotalCost_FP6 == METHOD_MIN){
        		calculateMinProjecTotalCost(xmlContentsFP6);
        	} else if(methodProjecTotalCost_FP6 == METHOD_MEAN){
        		calculateMeanProjecTotalCost(xmlContentsFP6);
        	}
        }    
        if(FP7process){
        	System.out.println("\nProcessing FP7 projects:");
        	xmlContentsFP7 	= readCordisZipFile(inputDir  + File.separator + FP7projectsZipfile, FP7);
        	if(methodProjecTotalCost_FP7 == METHOD_MIN){
        		calculateMinProjecTotalCost(xmlContentsFP7);
        	} else if(methodProjecTotalCost_FP7 == METHOD_MEAN){
        		calculateMeanProjecTotalCost(xmlContentsFP7);
        	}
        }
        if(H2020process){	
	        System.out.println("\nProcessing H2020 projects:");
	        xmlContentsH2020 = readCordisZipFile(inputDir  + File.separator + H2020projectsZipfile, H2020);
        	if(methodProjecTotalCost_H2020 == METHOD_MIN){
        		calculateMinProjecTotalCost(xmlContentsH2020);
        	} else if(methodProjecTotalCost_H2020 == METHOD_MEAN){
        		calculateMeanProjecTotalCost(xmlContentsH2020);
        	}
        }
     
        
        
		// write lda text file
        String filename = projectsFile;
        if(FP1process){
        	if(splitFiles){
        		filename = FP1projectsFile;
        	}       
        	writeLDATextFile(outputDir  + File.separator + filename + "-ldatext.csv", xmlContentsFP1);
        }
        if(FP2process){
        	if(splitFiles){
        		filename = FP2projectsFile;
        	}       
        	writeLDATextFile(outputDir  + File.separator + filename + "-ldatext.csv", xmlContentsFP2);
        }
        if(FP3process){
        	if(splitFiles){
        		filename = FP3projectsFile;
        	}       
        	writeLDATextFile(outputDir  + File.separator + filename + "-ldatext.csv", xmlContentsFP3);
        }
   
        if(FP4process){
        	if(splitFiles){
        		filename = FP4projectsFile;
        	}       
        	writeLDATextFile(outputDir  + File.separator + filename + "-ldatext.csv", xmlContentsFP4);
        }
        
        if(FP5process){
        	if(splitFiles){
        		filename = FP6projectsFile;
        	}       
        	writeLDATextFile(outputDir  + File.separator + filename + "-ldatext.csv", xmlContentsFP5);
        }
        if(FP6process){
        	if(splitFiles){
        		filename = FP6projectsFile;
        	}       
        	writeLDATextFile(outputDir  + File.separator + filename + "-ldatext.csv", xmlContentsFP6);
        }
        if(FP7process){
        	if(splitFiles){
        		filename = FP7projectsFile;
        	}
        	writeLDATextFile(outputDir  + File.separator + filename + "-ldatext.csv", xmlContentsFP7);
        }
        if(H2020process){
        	if(splitFiles){
        		filename = H2020projectsFile;
        	}
        	writeLDATextFile(outputDir  + File.separator + filename + "-ldatext.csv", xmlContentsH2020);
        }
        
        
		// write metadata file
        filename = projectsFile;
        if(FP1process){
        	if(splitFiles){
        		filename = FP1projectsFile;
        	}
        	writeMetadataFile(outputDir  + File.separator + filename + "-metadata.csv", xmlContentsFP1, FP1);
        }
        if(FP2process){
        	if(splitFiles){
        		filename = FP2projectsFile;
        	}
        	writeMetadataFile(outputDir  + File.separator + filename + "-metadata.csv", xmlContentsFP2, FP2);
        }
        if(FP3process){
        	if(splitFiles){
        		filename = FP3projectsFile;
        	}
        	writeMetadataFile(outputDir  + File.separator + filename + "-metadata.csv", xmlContentsFP3, FP3);
        }
        
        if(FP4process){
        	if(splitFiles){
        		filename = FP4projectsFile;
        	}
        	writeMetadataFile(outputDir  + File.separator + filename + "-metadata.csv", xmlContentsFP4, FP4);
        }
        
        if(FP5process){
        	if(splitFiles){
        		filename = FP5projectsFile;
        	}
        	writeMetadataFile(outputDir  + File.separator + filename + "-metadata.csv", xmlContentsFP5, FP5);
        }
        if(FP6process){
        	if(splitFiles){
        		filename = FP6projectsFile;
        	}
        	writeMetadataFile(outputDir  + File.separator + filename + "-metadata.csv", xmlContentsFP6, FP6);
        }     
        if(FP7process){
        	if(splitFiles){
        		filename = FP7projectsFile;
        	}
        	writeMetadataFile(outputDir  + File.separator + filename + "-metadata.csv", xmlContentsFP7, FP7);
        }      
        if(H2020process){
        	if(splitFiles){
        		filename = H2020projectsFile;
        	}        	
        	writeMetadataFile(outputDir  + File.separator + filename + "-metadata.csv", xmlContentsH2020, H2020);
        }
	}
	

	private static void calculateMeanProjecTotalCost(HashMap<Integer, List<String>> xmlContents) {
		// TODO xmlContents store objects not a list
		Double mean = 0d;
		int xmlContentsSize = xmlContents.size();
		
		// calculate mean
		int cnt_not_null = 0;
		for(Map.Entry<Integer,  List<String>> entry : xmlContents.entrySet()){
			List<String> content = entry.getValue();
			Double totalCost = Double.parseDouble(content.get(totalCostPosition));
			if(totalCost > 0){
				mean += totalCost;
				cnt_not_null++;
			}
		}
		if(cnt_not_null > 0){
			mean = mean/cnt_not_null;
		} else {
			return;
		}
		System.out.println("mean: " + mean);
		
		// replace mean
		for(Map.Entry<Integer,  List<String>> entry : xmlContents.entrySet()){
			List<String> content = entry.getValue();
			Double totalCost = Double.parseDouble(content.get(totalCostPosition));
			if(totalCost == 0){
				content.set(totalCostPosition, "" + df4.format(mean));//TODO format
			}
		}
	}
	
	private static void calculateMinProjecTotalCost(HashMap<Integer, List<String>> xmlContents) {
		// TODO xmlContents store objects not a list
		Double min = 1000000000d;
		
		// calculate min
		for(Map.Entry<Integer,  List<String>> entry : xmlContents.entrySet()){
			List<String> content = entry.getValue();
			Double totalCost = Double.parseDouble(content.get(totalCostPosition));
			if(totalCost > 0 && totalCost < min){
				min = totalCost;
			}
		}
		System.out.print("min: " + min);
		System.out.println();

		// replace min
		for(Map.Entry<Integer,  List<String>> entry : xmlContents.entrySet()){
			List<String> content = entry.getValue();
			Double totalCost = Double.parseDouble(content.get(totalCostPosition));
			if(totalCost == 0){
				content.set(totalCostPosition, "" + df4.format(min));//TODO format
			}
		}
	}


	private static HashMap<Integer, List<String>> readCordisCsvFile(HashMap<Integer, List<String>> csvContents) {
		HashMap<Integer, List<String>> contents = new HashMap<Integer, List<String>>(10000);
			
		for(Map.Entry<Integer,  List<String>> entry : csvContents.entrySet()){
			List<String> contentList = new ArrayList<>();
			int docid = processProjectCSV(entry.getValue(), contentList);
			if(docid > 0){
				contents.put(docid, contentList);
			}
		}

		return contents;
	}





	private static HashMap<Integer, List<String>> readCordisZipFile(String inputFile, int frameProgram) throws IOException, SAXException, XPathExpressionException {
    	HashMap<Integer, List<String>> xmlContents = new HashMap<Integer, List<String>>(10000);
    	ZipFile zipFile = new ZipFile(inputFile);
        Enumeration<? extends ZipEntry> entries = zipFile.entries();

        int cnt_xml_files = 0;
        int cnt_xml_error_files = 0;
        
        while(entries.hasMoreElements()){
            ZipEntry entry = entries.nextElement();
            
            if (!entry.isDirectory() && isProjectXMLfilename(entry.getName())) {            	
	            InputStream stream = zipFile.getInputStream(entry);            
	            Document doc = builder.parse(stream);
	            
	            printDot(cnt_xml_files);
	            
	            // process XML file
	            List<String> contents = new ArrayList<>();
	            int doc_id = processProjectXML(doc, contents, frameProgram);
	            if(doc_id > 0){
	            	xmlContents.put(doc_id, contents);
	            	cnt_xml_files++;
	            } else {
	            	System.out.println("\nError line: " + cnt_xml_files);
	            	cnt_xml_error_files++;
	            }
	            stream.close();          
            }
        }
        zipFile.close();
        
        System.out.println("\nxml files ok: " + cnt_xml_files + ", error files: " + cnt_xml_error_files);
        
		return xmlContents;
	}
	
	private static void writeLDATextFile(String filename, HashMap<Integer, List<String>> xmlContents) {
        FileOutputStream outputStream;
		try {
			outputStream = new FileOutputStream(new File(filename),true);//append
	        BufferedWriter stdWriter = new BufferedWriter(new OutputStreamWriter(outputStream,"UTF-8"));

	        // content
	        writeLDALines(xmlContents, stdWriter);
	        
			stdWriter.flush();
			stdWriter.close();
			outputStream.close();		
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static String processLDATextLine(List<String> listContent) {
		String ret = "";
		for(int i=0; i < ldaTextProjectsFields.length; i++){
			ret += cleanString(listContent.get(ldaTextProjectsFields[i])) + " ";
		}		
		return ret.trim().toLowerCase();//TODO toLower??
	}
	
	private static void writeLDALines(HashMap<Integer, List<String>> xmlContents, BufferedWriter stdWriter) {	
    	xmlContents.forEach((key,value) -> {
    		// accept text length
    	    String strLDATextLine =  processLDATextLine(value);
    	    if(removeShortTextProjects){
    	    	if(strLDATextLine.length() < shortTextProjectsThreshold){
    	    		shortTextProjectsList.add(key);
    	    		return; // only skips this iteration
    	    	}
    	    }
    	    
    	    // accept text lang
    	    if(removeOtherLangProjects){
    	    	if(langDetector == null){
    	    		langDetector = new OptimaizeLangDetector();    	    		
    	    		try {
//    	    			Set<String> langDetectionSet = new HashSet<>(Arrays.asList(mainLang));
//    	    			langDetector.loadModels(langDetectionSet);
    	    			langDetector.loadModels();// EU langs
					} catch (IOException e) {
						e.printStackTrace();
					}
    	    	}
    	    	langDetector.reset(); 	
    	    	langDetector.addText(strLDATextLine);
    	    	LanguageResult langDetection = langDetector.detect();
    	    	
    	    	if(!langDetection.getLanguage().toLowerCase().equals(mainLang)){
    	    		removeOtherLangProjectsList.add(key);
    	    		return;
    	    	}
    	    }
    	        	    
			try {
		        if(eu_us_comparative){
		        	stdWriter.write("EU" + key + "\t" + processLDATextLine(value) + "\n");
		        } else {
		        	stdWriter.write(key + "\t" + processLDATextLine(value) + "\n");
		        }
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}
	
	private static void writeMetadataFile(String filename, HashMap<Integer, List<String>> xmlContents, int frameProgram) {
        FileOutputStream outputStream;
		try {
			outputStream = new FileOutputStream(new File(filename),true);//append
	        BufferedWriter stdWriter = new BufferedWriter(new OutputStreamWriter(outputStream,"UTF-8"));
			
	        // header	        
	        stdWriter.write(processMetadataHeader() + "\n");
	        
	        // content
	        if(frameProgram == FP1){
	        	writeMetadataLines(xmlContents, "FP1"  , stdWriter);
	        } else if(frameProgram == FP2){
	        	writeMetadataLines(xmlContents, "FP2"  , stdWriter);
	        } else if(frameProgram == FP3){
	        	writeMetadataLines(xmlContents, "FP3"  , stdWriter);
	        } else if(frameProgram == FP4){
	        	writeMetadataLines(xmlContents, "FP4"  , stdWriter);	        	
	        } else if(frameProgram == FP5){
	        	writeMetadataLines(xmlContents, "FP5"  , stdWriter);
	        } else if(frameProgram == FP6){
	        	writeMetadataLines(xmlContents, "FP6"  , stdWriter);
	        } else if(frameProgram == FP7){
	        	writeMetadataLines(xmlContents, "FP7"  , stdWriter);
	        } else if(frameProgram == H2020){
	        	writeMetadataLines(xmlContents, "H2020", stdWriter);
	        } 
	        
			stdWriter.flush();
			stdWriter.close();
			outputStream.close();		
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void writeMetadataLines(HashMap<Integer, List<String>> xmlContents, String frameProgram, BufferedWriter stdWriter) {
		xmlContents.forEach((key,value) -> {
    	    if(removeShortTextProjects){
    	    	if(shortTextProjectsList.contains(key)){
    	    		return; // only skips this iteration
    	    	}
    	    }
    	    if(removeOtherLangProjects){
    	    	if(removeOtherLangProjectsList.contains(key)){
    	    		return; 
    	    	}
    	    }
    	    
			try {
				if(eu_us_comparative){
					stdWriter.write("EU" + key + "" + DEFAULT_SEPARATOR + "\"EU\"" + processMetadataLine(value) + "\n");
				} else {
					stdWriter.write(key + "" + DEFAULT_SEPARATOR + frameProgram + processMetadataLine(value) + "\n");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	private static String processMetadataHeader() {
		String ret = "Label";
		if(eu_us_comparative){
			ret += DEFAULT_SEPARATOR +"Area";
		} else {
			ret += DEFAULT_SEPARATOR +"frameProgram";
		}
		for(int i=0; i < metadataProjectsFields.length; i++){
			ret += DEFAULT_SEPARATOR + metadataProjectsHeaders[metadataProjectsFields[i]];
		}		
		return ret;
	}
	
	private static String processMetadataLine(List<String> listContent) {
		String ret = "";
		for(int i=0; i < metadataProjectsFields.length; i++){
			ret += DEFAULT_SEPARATOR + "\"" + listContent.get(metadataProjectsFields[i]).toLowerCase() + "\"";//TODO toLower
		}		
		return ret;
	}


	private static int processProjectCSV(List<String> inputLine, List<String> contents) {
		if (inputLine.size() > 0) {
			//RCN;Project Title;Start Date;End Date;Duration;Status;Contract Number;Keywords;Date of Signature;Total Cost;
			// Total Funding; Project Website;Project Call;Project Acronym;General Information;Achievements;Objectives;Activity Area;Contract Type;Subject;
			// Framework Programme;PGA;Coordinator Country;Contractor Country
			
			// ver processProjectXML
			// check project lang (cdl), No existe lang 
			
	        // id
	        String projectName = (String) inputLine.get(0);
	        int projectId = Integer.parseInt(projectName);
	        
	        // acronym
	        String projectAcronym = (String) inputLine.get(13);
	        contents.add(projectAcronym);   
	       
	        // teaser
	        String projectTeaser = "";
	        //projectTeaser = cleanString(projectTeaser);
	        contents.add(projectTeaser);   
	        
	        // Objective
	        String projectObjective = ((String) inputLine.get(14)) + " " + ((String) inputLine.get(15)) + " " + ((String) inputLine.get(16)); // 17 Activity area, 19 subject (split ;)??
	        projectObjective = cleanFP4Strings(projectObjective);
	        projectObjective = cleanString(projectObjective);
	        if(projectObjective.trim().length() == 0){
	        	System.out.println("\nProject with null object length, id: " + projectId);
	        	return 0;
	        }
	        contents.add(projectObjective);  
	        
	        // Title
	        String projectTitle = (String) inputLine.get(1);
	        projectTitle = cleanString(projectTitle);
	        contents.add(projectTitle);  
	        
	        // TotalCost
	        String projectTotalCost = (String) inputLine.get(9);
	        projectTotalCost = cleanString(projectTotalCost);
	        projectTotalCost = projectTotalCost.replaceAll(" ", "").replaceAll(",", "."); 
	        if((projectTotalCost.trim().length() == 0)||((methodProjecTotalCost_FP4 == METHOD_STATIC_MIN) && (Double.parseDouble(projectTotalCost) < minProjecTotalCost))){
	        	if(methodProjecTotalCost_FP4 == METHOD_STATIC_MIN){//get method
	        		projectTotalCost = ""+ minProjecTotalCost;
	        	} else {
	        		projectTotalCost = ""+ 0;
	        	}
	        }
	        
	        // EcMaxContribution
	        String projectEcMaxContribution = (String) inputLine.get(10);
	        projectEcMaxContribution = cleanString(projectEcMaxContribution);
	        projectEcMaxContribution = projectEcMaxContribution.replaceAll(" ", "").replaceAll(",", ".");
	        if(projectEcMaxContribution.trim().length() == 0){
	        	projectEcMaxContribution = "0";
	        }       
	        
	        // get Max
	        double iProjectTotalCost = Double.parseDouble(projectTotalCost);
	        double iProjectEcMaxContribution = Double.parseDouble(projectEcMaxContribution);
	        
	        if(eu_us_comparative){
	        	double total_cost = Math.max(iProjectTotalCost, iProjectEcMaxContribution);
	            contents.add(""+ df4.format(total_cost));
	            contents.add(""+ df4.format(total_cost));
	        } else {
	            contents.add(projectTotalCost);  
	            contents.add(projectEcMaxContribution);         	
	        }
	        
	        // StartDate
	        String projectStartDate = (String) inputLine.get(2);
	        projectStartDate = checkDate(projectStartDate, true, FP4);
	        contents.add(projectStartDate);  
	       
	        // EndDate
	        String projectEndDate = (String) inputLine.get(3);
	        projectEndDate = checkDate(projectEndDate, false, FP4);
	        contents.add(projectEndDate);  
	        
	        // ContractDuration
	        String projectContractDuration = (String) inputLine.get(4);
	        if(projectContractDuration.length() == 0){
	        	projectContractDuration = "" + Math.min(maxProjectDurationYears, getProjectDuration(projectStartDate, projectEndDate));
	        }
	        contents.add(projectContractDuration);  

	        
	        
	        // Status
	        String projectStatus = (String) inputLine.get(5);
	        contents.add(projectStatus);  
	        
			return projectId;
		} else {
			return 0;
		}	
	}
	
	private static String cleanFP4Strings(String projectObjective) {
		for(int i=0; i < FP4Strings.length; i++){
			projectObjective = projectObjective.replaceAll(FP4Strings[i], " ");
		}
		return projectObjective;
	}


	private static int processProjectXML(Document doc, List<String> contents, int frameProgram) throws XPathExpressionException {
        // lang 
        String projectLang = (String) exprProjectLanguage.evaluate(doc, XPathConstants.STRING);
        if(!projectLang.toLowerCase().trim().equals("en")){
        	System.out.println("\nProject lang error: " + projectLang);
        	return 0;
        }
        
        // id
        String projectName = (String) exprProjectName.evaluate(doc, XPathConstants.STRING);
        int projectId = Integer.parseInt(projectName);
        //contents.add(projectName);
        
        // acronym
        String projectAcronym = (String) exprProjectAcronym.evaluate(doc, XPathConstants.STRING);
        contents.add(projectAcronym);   
       
        // teaser
        String projectTeaser = (String) exprProjectTeaser.evaluate(doc, XPathConstants.STRING);
        projectTeaser = cleanString(projectTeaser);
        contents.add(projectTeaser);   
        
        // Objective
        String projectObjective = (String) exprProjectObjective.evaluate(doc, XPathConstants.STRING);
        projectObjective = cleanString(projectObjective);
        contents.add(projectObjective);  
        
        // Title
        String projectTitle = (String) exprProjectTitle.evaluate(doc, XPathConstants.STRING);
        projectTitle = cleanString(projectTitle);
        contents.add(projectTitle);  
        
//        if(projectName.equals("94504"))
//        	System.out.println();

        // TotalCost
        String projectTotalCost = (String) exprProjectTotalCost.evaluate(doc, XPathConstants.STRING);
        projectTotalCost = projectTotalCost.replaceAll(" ", "").replaceAll(",", ".");
                
        if((projectTotalCost.trim().length() == 0)||((getProjectTotalCostMethod(frameProgram) == METHOD_STATIC_MIN) && (Double.parseDouble(projectTotalCost) < minProjecTotalCost))){
        	if(getProjectTotalCostMethod(frameProgram) == METHOD_STATIC_MIN){
        		projectTotalCost = ""+ minProjecTotalCost;
        	} else {
        		projectTotalCost = ""+ 0;
        	}
        }
        
        // EcMaxContribution
        String projectEcMaxContribution = (String) exprProjectEcMaxContribution.evaluate(doc, XPathConstants.STRING);
        projectEcMaxContribution = projectEcMaxContribution.replaceAll(" ", "").replaceAll(",", ".");
        if(projectEcMaxContribution.trim().length() == 0){
        	projectEcMaxContribution = "0";
        }       
        
        
        // get Max
        double iProjectTotalCost = Double.parseDouble(projectTotalCost);
        double iProjectEcMaxContribution = Double.parseDouble(projectEcMaxContribution);
        
        if(eu_us_comparative){
        	double total_cost = Math.max(iProjectTotalCost, iProjectEcMaxContribution);
            contents.add(""+ df4.format(total_cost));
            contents.add(""+ df4.format(total_cost));
        } else {
            contents.add(projectTotalCost);  
            contents.add(projectEcMaxContribution);         	
        }
        
        // StartDate
        String projectStartDate = (String) exprProjectStartDate.evaluate(doc, XPathConstants.STRING);
        projectStartDate = checkDate(projectStartDate, true, frameProgram);
        contents.add(projectStartDate);  
       
        // EndDate
        String projectEndDate = (String) exprProjectEndDate.evaluate(doc, XPathConstants.STRING);
        projectEndDate = checkDate(projectEndDate, false, frameProgram);
        contents.add(projectEndDate);  
        
        // ContractDuration
        String projectContractDuration;
        if(frameProgram == FP7||frameProgram == H2020){
        	projectContractDuration = (String) exprProjectContractDuration.evaluate(doc, XPathConstants.STRING);
        } else {
        	projectContractDuration = "" + Math.min(maxProjectDurationYears, getProjectDuration(projectStartDate, projectEndDate));
        }
        contents.add(projectContractDuration);  
        
        // Status
        String projectStatus;
        
        if(frameProgram == H2020){
        	projectStatus = (String) exprProjectStatus.evaluate(doc, XPathConstants.STRING);
        } else {
        	projectStatus = "SIGNED";
        }
        contents.add(projectStatus);  
		return projectId;
	}
	
	

	private static int getProjectTotalCostMethod(int frameProgram) {
		if(frameProgram == FP1){
        	return methodProjecTotalCost_FP1;
        } else if(frameProgram == FP2){
        	return methodProjecTotalCost_FP2;
        } else if(frameProgram == FP3){
        	return methodProjecTotalCost_FP3;
        } else if(frameProgram == FP4){
        	return methodProjecTotalCost_FP4;        	
        } else if(frameProgram == FP5){
        	return methodProjecTotalCost_FP5;
        } else if(frameProgram == FP6){
        	return methodProjecTotalCost_FP6;
        } else if(frameProgram == FP7){
        	return methodProjecTotalCost_FP7;
        } else if(frameProgram == H2020){
        	return methodProjecTotalCost_H2020;
        }
		return methodProjecTotalCost_H2020; 
	}


	private static int getProjectDuration(String projectStartDate, String projectEndDate) {
		String[] partsStartDate = projectStartDate.split("-");
		String[] partsEndDate = projectEndDate.split("-");
		
		int startYear = Integer.parseInt(partsStartDate[0]);
		int endYear = Integer.parseInt(partsEndDate[0]);
		
		return endYear - startYear;
	}


	private static boolean isProjectXMLfilename(String filename) {
		if(filename.toLowerCase().contains("project")){
			return true;
		}
		return false;
	}
	
	// check dates
	private static String checkDate(String inputDate, boolean startDate, int frameProgram) {
		// format 1986-11-26
		
		// TODO use java Date to validate date
		String[] parts = inputDate.split("-");
		if(parts.length != 3){
			return getLimitDates(frameProgram, startDate);
		}
		if(startDate){
			if(Integer.parseInt(parts[0]) < getLimitYear(frameProgram, true)){
				return getLimitDates(frameProgram, startDate);
			}
		} else {
			if(Integer.parseInt(parts[0]) > getLimitYear(frameProgram, false) + getMaxProjectDurationYears(frameProgram)){
				return getLimitDates(frameProgram, startDate);
			}
		} 
		return inputDate;
	}

	
	private static int getLimitYear(int frameProgram, boolean start) {
		//		FP1 (1984–1987)
		//		FP2 (1987–1991)
		//		FP3 (1990–1994)
		//		FP4 (1994-1998)
		//		FP5 (1998-2002)
		//		FP6 (2002-2006)
		//		FP7 (2007-2013)
		//		Horizon 2020 (2014-2020)
		
		if(frameProgram == FP1){
        	return (start)?1984:1987;
        } else if(frameProgram == FP2){
        	return (start)?1987:1991;
        } else if(frameProgram == FP3){
        	return (start)?1990:1994;
        } else if(frameProgram == FP4){
        	return (start)?1994:1998;	        	
        } else if(frameProgram == FP5){
        	return (start)?1998:2002;
        } else if(frameProgram == FP6){
        	return (start)?2002:2006;
        } else if(frameProgram == FP7){
        	return (start)?2007:2013;
        } else if(frameProgram == H2020){
        	return (start)?2014:2020;
        }
		return (start)?2014:2020; 
	}
	
	private static String getLimitDates(int frameProgram, boolean start) {
		//		FP1 (1984–1987)
		//		FP2 (1987–1991)
		//		FP3 (1990–1994)
		//		FP4 (1994-1998)
		//		FP5 (1998-2002)
		//		FP6 (2002-2006)
		//		FP7 (2007-2013)
		//		Horizon 2020 (2014-2020)
		
		if(frameProgram == FP1){
        	return (start)?"1984-1-1":"1986-12-31";//TODO real limit dates
        } else if(frameProgram == FP2){
        	return (start)?"1987-1-1":"1991-12-31";
        } else if(frameProgram == FP3){
        	return (start)?"1990-1-1":"1993-12-31";
        } else if(frameProgram == FP4){
        	return (start)?"1994-1-1":"1997-12-31";	        	
        } else if(frameProgram == FP5){
        	return (start)?"1998-1-1":"2001-12-31";
        } else if(frameProgram == FP6){
        	return (start)?"2002-1-1":"2006-12-31";
        } else if(frameProgram == FP7){
        	return (start)?"2007-1-1":"2013-12-31";
        } else if(frameProgram == H2020){
        	return (start)?"2014-1-1":"2020-12-31";
        }
		return (start)?"2014-1-1":"2020-12-31"; 
	}

	private static int getMaxProjectDurationYears(int frameProgram) {
		return maxProjectDurationYears;
	}

	// Util 
	private static String cleanString(String field) {
		String cleanString = field.replaceAll("\\P{InBasic_Latin}", "");
		cleanString = cleanString.replaceAll("\n", " ");
		cleanString = cleanString.replaceAll("[\\.\\*\\:]+", " ");
		cleanString = cleanString.replaceAll("\"", "");
		cleanString = cleanString.replaceAll("[ \t]+"," ");
		cleanString = cleanString.replaceAll("[\\.]+",".");

		return cleanString.trim();
	}	
	
	private static void printDot(int cnt_xml_files) {
        if(cnt_xml_files > 0 && cnt_xml_files%50 == 0){
        	System.out.print(".");
			if(cnt_xml_files%5000 == 0){
				 System.out.print("\n");
			}
        }		
	}
	
	public static void deleteFolder(File folder, boolean recursive) {
        File[] files = folder.listFiles();
        if(files!=null) { //some JVMs return null for empty dirs
            for(File f: files) {
            	if(recursive){
	                if(f.isDirectory()) {
	                    deleteFolder(f, recursive);
	                } else {
	                    f.delete();
	                }
            	} else {
            		deleteFolder(f,recursive);
            	}
            }
        }
        folder.delete();
    } 	
	
    private static void initXPathExpressions() throws XPathExpressionException {
        exprProjectName = xpath.compile("/project/rcn/text()");
        exprProjectLanguage = xpath.compile("/project/language/text()");
        exprProjectAcronym = xpath.compile("/project/acronym/text()");
        exprProjectTeaser = xpath.compile("/project/teaser/text()");
        exprProjectObjective = xpath.compile("/project/objective/text()");
        exprProjectTitle = xpath.compile("/project/title/text()");
        exprProjectTotalCost = xpath.compile("/project/totalCost/text()");
        exprProjectEcMaxContribution = xpath.compile("/project/ecMaxContribution/text()");
        exprProjectStartDate = xpath.compile("/project/startDate/text()");
        exprProjectEndDate = xpath.compile("/project/endDate/text()");
        exprProjectContractDuration = xpath.compile("/project/contract/duration/text()");
        exprProjectStatus = xpath.compile("/project/status/text()");
	}
}
