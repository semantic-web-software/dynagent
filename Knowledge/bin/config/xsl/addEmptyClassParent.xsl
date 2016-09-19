<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="xml" indent="yes" omit-xml-declaration="no" />
	<xsl:template match="/">
		<CONFIG>
			<COLUMNPROP>
				<xsl:apply-templates />
			</COLUMNPROP>
		</CONFIG>
	</xsl:template>
	
	<xsl:template match="COLUMNPROP">
		<xsl:for-each select="CP">
			<CP>
				<xsl:attribute name="CLASS"><xsl:value-of select="@CLASS" /></xsl:attribute>
				<xsl:attribute name="CLASSPARENT">
					<xsl:if test="@CLASSPARENT">
						<xsl:value-of select="@CLASSPARENT" />
					</xsl:if>
				</xsl:attribute>
				<xsl:attribute name="PROP"><xsl:value-of select="@PROP" /></xsl:attribute>
				<xsl:attribute name="PRIORITY"><xsl:value-of select="@PRIORITY" /></xsl:attribute>
			</CP>
		</xsl:for-each>
	</xsl:template>
</xsl:stylesheet>