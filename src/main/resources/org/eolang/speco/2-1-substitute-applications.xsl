<?xml version="1.0" encoding="UTF-8"?>
<!--
The MIT License (MIT)

Copyright (c) 2022 Objectionary

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included
in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" id="SG" version="2.0">
  <!--
    Rule #2: substitute all applications of native objects with
    applications of their synthetic counterparts.
    @todo #47:30min add unit-test for transformation,
     in which the input will be xmir with applications with polymorphic objects and
     the expected result will be the same xmir with an applications with their
     specialized versions.
  -->
  <xsl:output indent="yes" method="xml"/>
  <xsl:strip-space elements="*"/>
  <!--
    Checks the appropriate specialized version in the lines with the application.
  -->
  <xsl:template match="/program/objects//o">
    <xsl:variable name="name" select="@base"/>
    <xsl:variable name="spec">
      <xsl:for-each select="o">
        <xsl:copy>
          <xsl:value-of select="concat(@base, '_')"/>
        </xsl:copy>
      </xsl:for-each>
    </xsl:variable>
    <xsl:copy>
      <xsl:if test="@base">
        <xsl:attribute name="base">
          <xsl:value-of select="$name"/>
        </xsl:attribute>
      </xsl:if>
      <xsl:for-each select="/program/speco/version[@name=$name and concat(@spec, '_')=$spec]/o">
        <xsl:attribute name="base">
          <xsl:value-of select="@name"/>
        </xsl:attribute>
      </xsl:for-each>
      <xsl:apply-templates select="@* except @base |node()"/>
    </xsl:copy>
  </xsl:template>
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>
</xsl:stylesheet>