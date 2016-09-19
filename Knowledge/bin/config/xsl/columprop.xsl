<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="xml" indent="yes" omit-xml-declaration="no" />
	<xsl:template match="/">
		<COLUMNPROP>
			<xsl:apply-templates />
		</COLUMNPROP>
	</xsl:template>
	
	<xsl:template match="COLUMNPROP" name="withoutClass">
		<xsl:for-each-group select="CP" group-by="@CLASS">
			<xsl:for-each-group select="current-group()" group-by="@CLASSPARENT">
				<CPROP>
					<xsl:attribute name="CLASS">
						<xsl:value-of select="@CLASS" />
					</xsl:attribute>
					<xsl:if test="@CLASSPARENT != ''">
						<xsl:attribute name="CLASSPARENT">
							<xsl:value-of select="current-grouping-key()" />
						</xsl:attribute>
					</xsl:if>
					<xsl:for-each select="current-group()">
						<CP>
							<xsl:attribute name="PROP">
								<xsl:value-of select="@PROP" />
							</xsl:attribute>
							<xsl:attribute name="ORDER">
								<xsl:value-of select="@PRIORITY" />
							</xsl:attribute>
						</CP>
					</xsl:for-each>
				</CPROP>
			</xsl:for-each-group>
		</xsl:for-each-group>
	</xsl:template>
</xsl:stylesheet>