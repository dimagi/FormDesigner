	//var xsl_string = '<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"> <xsl:output omit-xml-declaration="yes" indent="yes"/>    <xsl:template match="node()|@*">      <xsl:copy>        <xsl:apply-templates select="node()|@*"/>      </xsl:copy>    </xsl:template></xsl:stylesheet>';


	// from: http://www.xml.com/pub/a/2006/11/29/xslt-xml-pretty-printer.html?page=3

	var xsl_string = '<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">\
	<xsl:output method="xml" indent="yes"/>\
	<xsl:strip-space elements="*"/>\
	<xsl:template match="/">\
	  <xsl:copy-of select="."/>\
	</xsl:template>\
	</xsl:stylesheet>';


	var xsl = (new DOMParser()).parseFromString(xsl_string, "text/xml");


	function stringToXml(xml_string) {
		return (new DOMParser()).parseFromString(xml_string, "text/xml");
	}

	function xmlToString(xml) {
		return (new XMLSerializer()).serializeToString(xml);
	}


	function isParseError(xml) {
		try {
		   // console.log(     xml.documentElement.firstChild.firstChild.tagName);
			return xml.documentElement.tagName == "parsererror" ||
					xml.documentElement.firstChild.firstChild.tagName == "parsererror";
		}
		catch (ex) {
			return false;
		}
	}
	function beautifyXml(input) {
		var xml = stringToXml(input);

		if (isParseError(xml)) {
			return input;
		}

		var transformedXml = xslTransformation(xml, xsl);
		return xmlToString(transformedXml);
	}

	/**
	 * @param xml
	 * @param xsl
	 */
	function xslTransformation(xml, xsl) {
		// code for IE
		if (window.ActiveXObject) {
			var ex = xml.transformNode(xsl);
			return ex;
		}
		// code for Mozilla, Firefox, Opera, etc.
		else if (document.implementation && document.implementation.createDocument) {
			var xsltProcessor = new XSLTProcessor();
			xsltProcessor.importStylesheet(xsl);
			var resultDocument = xsltProcessor.transformToDocument(xml, document);
			return resultDocument;
		}
	}