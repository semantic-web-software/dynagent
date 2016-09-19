<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="xml" indent="yes" omit-xml-declaration="no" />
	<xsl:template match="/">
		<GROUPS>
			<xsl:apply-templates />
		</GROUPS>
	</xsl:template>
	
	<xsl:template match="GROUPS">
		<xsl:for-each-group select="GP" group-by="@GROUP">
			<GROUP>
				<xsl:attribute name="NAME"><xsl:value-of select="@GROUP" /></xsl:attribute>
				<xsl:for-each select="current-group()">
					<GP>
						<xsl:attribute name="PROP"><xsl:value-of select="@PROP" /></xsl:attribute>
					</GP>
				</xsl:for-each>
			</GROUP>
		</xsl:for-each-group>
	</xsl:template>
</xsl:stylesheet>