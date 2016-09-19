<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="xml" indent="yes" omit-xml-declaration="no" />
	<xsl:template match="/">
		<ORDERPROPERTIES>
			<xsl:apply-templates />
		</ORDERPROPERTIES>
	</xsl:template>
	
	<xsl:template match="ORDERPROPERTIES">
		<xsl:for-each-group select="OP" group-by="@SEC">
			<OPROP>
				<xsl:for-each select="current-group()">
					<OP>
						<xsl:attribute name="PROP"><xsl:value-of select="@PROP" /></xsl:attribute>
						<xsl:attribute name="ORDER"><xsl:value-of select="@ORDER" /></xsl:attribute>
					</OP>
				</xsl:for-each>
			</OPROP>
		</xsl:for-each-group>
	</xsl:template>
</xsl:stylesheet>