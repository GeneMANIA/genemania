package org.genemania.adminweb.service.impl;

import java.io.IOException;
import java.io.PrintStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.genemania.adminweb.exception.DatamartException;
import org.genemania.adminweb.service.PubmedService;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

@Component
public class PubmedServiceImpl implements PubmedService {

    public static final String PUBMED_URL = "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?id=%s&db=pubmed&retmode=xml";

    @Override
    public PubmedInfo getInfo(long pubmedId) throws DatamartException {
        try {
            Document document = getDoc(pubmedId);
            //dump(document, System.out);
            return getInfo(document);
        }
        catch (Exception e) {
            throw new DatamartException("failed to get pubmed id", e);
        }
    }

    public Document getDoc(long pubmedId) throws SAXException, IOException,
        ParserConfigurationException, TransformerException {

        DocumentBuilderFactory factory =
                DocumentBuilderFactory.newInstance();

        factory.setValidating(true);
        factory.setNamespaceAware(false);

        DocumentBuilder builder = factory.newDocumentBuilder();

        String url = String.format(PUBMED_URL, pubmedId);
        Document document = builder.parse(url);

        return document;


    }

    public static class PubmedInfo {
        String title;
        String year;
        String faln; // first author last name
        String laln; // last author last name

        public String getTitle() {
            return title;
        }
        public void setTitle(String title) {
            this.title = title;
        }
        public String getYear() {
            return year;
        }
        public void setYear(String year) {
            this.year = year;
        }
        public String getFaln() {
            return faln;
        }
        public void setFaln(String faln) {
            this.faln = faln;
        }
        public String getLaln() {
            return laln;
        }
        public void setLaln(String laln) {
            this.laln = laln;
        }

    }

    public PubmedInfo getInfo(Document document) throws XPathExpressionException {

        PubmedInfo pubmedInfo = new PubmedInfo();

        XPathFactory xpfactory = XPathFactory.newInstance();
        XPath xpath = xpfactory.newXPath();
        XPathExpression expr = null;

        expr = xpath.compile("//ArticleTitle/text()");
        pubmedInfo.title = expr.evaluate(document, XPathConstants.STRING).toString();

        expr = xpath.compile("//Author/LastName");
        NodeList authors = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
//        System.out.println("# authors: " + authors.getLength());
//        for (int i=0; i<authors.getLength(); i++) {
//            Node author = authors.item(i);
//            System.out.println(author.getTextContent());
//        }

        if (authors.getLength() > 0) {
            pubmedInfo.faln = authors.item(0).getTextContent();
        }
        if (authors.getLength() > 1) {
            pubmedInfo.laln = authors.item(authors.getLength() - 1).getTextContent();
        }

        expr = xpath.compile("//PubDate/Year/text()");
        String year = expr.evaluate(document, XPathConstants.STRING).toString();
        if (year == null || year == "") {
            expr = xpath.compile("//MedlineDate/text()");
            year = expr.evaluate(document, XPathConstants.STRING).toString();
        }

        pubmedInfo.year = year;

        return pubmedInfo;

    }
    /*
     * debugging
     */
    private void dump(Document document, PrintStream printStream) throws TransformerException {
        Source source = new DOMSource(document);
        Result result = new StreamResult(printStream);
        TransformerFactory transformerFactory = TransformerFactory
                .newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.transform(source, result);
    }
}
