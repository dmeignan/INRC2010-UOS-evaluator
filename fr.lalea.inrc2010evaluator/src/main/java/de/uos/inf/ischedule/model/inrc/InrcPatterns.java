//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2013.06.28 at 12:26:20 PM CEST 
//


package de.uos.inf.ischedule.model.inrc;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for Patterns complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Patterns">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence maxOccurs="unbounded" minOccurs="0">
 *         &lt;element name="Pattern">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;all>
 *                   &lt;element name="PatternEntries">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;sequence maxOccurs="unbounded" minOccurs="2">
 *                             &lt;element name="PatternEntry">
 *                               &lt;complexType>
 *                                 &lt;complexContent>
 *                                   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                                     &lt;all>
 *                                       &lt;element name="ShiftType" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                                       &lt;element name="Day" type="{}WeekDayOrAny"/>
 *                                     &lt;/all>
 *                                     &lt;attribute name="index" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *                                   &lt;/restriction>
 *                                 &lt;/complexContent>
 *                               &lt;/complexType>
 *                             &lt;/element>
 *                           &lt;/sequence>
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                 &lt;/all>
 *                 &lt;attribute name="weight" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *                 &lt;attribute name="ID" type="{http://www.w3.org/2001/XMLSchema}string" />
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Patterns", propOrder = {
    "pattern"
})
public class InrcPatterns {

    @XmlElement(name = "Pattern")
    protected List<InrcPatterns.InrcPattern> pattern;

    /**
     * Gets the value of the pattern property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the pattern property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPattern().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link InrcPatterns.InrcPattern }
     * 
     * 
     */
    public List<InrcPatterns.InrcPattern> getPattern() {
        if (pattern == null) {
            pattern = new ArrayList<InrcPatterns.InrcPattern>();
        }
        return this.pattern;
    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;all>
     *         &lt;element name="PatternEntries">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;sequence maxOccurs="unbounded" minOccurs="2">
     *                   &lt;element name="PatternEntry">
     *                     &lt;complexType>
     *                       &lt;complexContent>
     *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                           &lt;all>
     *                             &lt;element name="ShiftType" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *                             &lt;element name="Day" type="{}WeekDayOrAny"/>
     *                           &lt;/all>
     *                           &lt;attribute name="index" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
     *                         &lt;/restriction>
     *                       &lt;/complexContent>
     *                     &lt;/complexType>
     *                   &lt;/element>
     *                 &lt;/sequence>
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *       &lt;/all>
     *       &lt;attribute name="weight" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
     *       &lt;attribute name="ID" type="{http://www.w3.org/2001/XMLSchema}string" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {

    })
    public static class InrcPattern {

        @XmlElement(name = "PatternEntries", required = true)
        protected InrcPatterns.InrcPattern.InrcPatternEntries patternEntries;
        @XmlAttribute(name = "weight")
        @XmlSchemaType(name = "anySimpleType")
        protected String weight;
        @XmlAttribute(name = "ID")
        protected String id;

        /**
         * Gets the value of the patternEntries property.
         * 
         * @return
         *     possible object is
         *     {@link InrcPatterns.InrcPattern.InrcPatternEntries }
         *     
         */
        public InrcPatterns.InrcPattern.InrcPatternEntries getPatternEntries() {
            return patternEntries;
        }

        /**
         * Sets the value of the patternEntries property.
         * 
         * @param value
         *     allowed object is
         *     {@link InrcPatterns.InrcPattern.InrcPatternEntries }
         *     
         */
        public void setPatternEntries(InrcPatterns.InrcPattern.InrcPatternEntries value) {
            this.patternEntries = value;
        }

        /**
         * Gets the value of the weight property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getWeight() {
            return weight;
        }

        /**
         * Sets the value of the weight property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setWeight(String value) {
            this.weight = value;
        }

        /**
         * Gets the value of the id property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getID() {
            return id;
        }

        /**
         * Sets the value of the id property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setID(String value) {
            this.id = value;
        }


        /**
         * <p>Java class for anonymous complex type.
         * 
         * <p>The following schema fragment specifies the expected content contained within this class.
         * 
         * <pre>
         * &lt;complexType>
         *   &lt;complexContent>
         *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         *       &lt;sequence maxOccurs="unbounded" minOccurs="2">
         *         &lt;element name="PatternEntry">
         *           &lt;complexType>
         *             &lt;complexContent>
         *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         *                 &lt;all>
         *                   &lt;element name="ShiftType" type="{http://www.w3.org/2001/XMLSchema}string"/>
         *                   &lt;element name="Day" type="{}WeekDayOrAny"/>
         *                 &lt;/all>
         *                 &lt;attribute name="index" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
         *               &lt;/restriction>
         *             &lt;/complexContent>
         *           &lt;/complexType>
         *         &lt;/element>
         *       &lt;/sequence>
         *     &lt;/restriction>
         *   &lt;/complexContent>
         * &lt;/complexType>
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
            "patternEntry"
        })
        public static class InrcPatternEntries {

            @XmlElement(name = "PatternEntry", required = true)
            protected List<InrcPatterns.InrcPattern.InrcPatternEntries.InrcPatternEntry> patternEntry;

            /**
             * Gets the value of the patternEntry property.
             * 
             * <p>
             * This accessor method returns a reference to the live list,
             * not a snapshot. Therefore any modification you make to the
             * returned list will be present inside the JAXB object.
             * This is why there is not a <CODE>set</CODE> method for the patternEntry property.
             * 
             * <p>
             * For example, to add a new item, do as follows:
             * <pre>
             *    getPatternEntry().add(newItem);
             * </pre>
             * 
             * 
             * <p>
             * Objects of the following type(s) are allowed in the list
             * {@link InrcPatterns.InrcPattern.InrcPatternEntries.InrcPatternEntry }
             * 
             * 
             */
            public List<InrcPatterns.InrcPattern.InrcPatternEntries.InrcPatternEntry> getPatternEntry() {
                if (patternEntry == null) {
                    patternEntry = new ArrayList<InrcPatterns.InrcPattern.InrcPatternEntries.InrcPatternEntry>();
                }
                return this.patternEntry;
            }


            /**
             * <p>Java class for anonymous complex type.
             * 
             * <p>The following schema fragment specifies the expected content contained within this class.
             * 
             * <pre>
             * &lt;complexType>
             *   &lt;complexContent>
             *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *       &lt;all>
             *         &lt;element name="ShiftType" type="{http://www.w3.org/2001/XMLSchema}string"/>
             *         &lt;element name="Day" type="{}WeekDayOrAny"/>
             *       &lt;/all>
             *       &lt;attribute name="index" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "", propOrder = {

            })
            public static class InrcPatternEntry {

                @XmlElement(name = "ShiftType", required = true)
                protected String shiftType;
                @XmlElement(name = "Day", required = true)
                protected String day;
                @XmlAttribute(name = "index")
                @XmlSchemaType(name = "anySimpleType")
                protected String index;

                /**
                 * Gets the value of the shiftType property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getShiftType() {
                    return shiftType;
                }

                /**
                 * Sets the value of the shiftType property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setShiftType(String value) {
                    this.shiftType = value;
                }

                /**
                 * Gets the value of the day property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getDay() {
                    return day;
                }

                /**
                 * Sets the value of the day property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setDay(String value) {
                    this.day = value;
                }

                /**
                 * Gets the value of the index property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getIndex() {
                    return index;
                }

                /**
                 * Sets the value of the index property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setIndex(String value) {
                    this.index = value;
                }

            }

        }

    }

}
