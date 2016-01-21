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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for CoverRequirements complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CoverRequirements">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice maxOccurs="unbounded" minOccurs="0">
 *         &lt;element name="DayOfWeekCover">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="Day" type="{}WeekDay"/>
 *                   &lt;element name="Cover" type="{}Cover" maxOccurs="unbounded"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="DateSpecificCover">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="Date" type="{http://www.w3.org/2001/XMLSchema}date"/>
 *                   &lt;element name="Cover" type="{}Cover" maxOccurs="unbounded"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CoverRequirements", propOrder = {
    "dayOfWeekCoverOrDateSpecificCover"
})
public class InrcCoverRequirements {

    @XmlElements({
        @XmlElement(name = "DayOfWeekCover", type = InrcCoverRequirements.InrcDayOfWeekCover.class),
        @XmlElement(name = "DateSpecificCover", type = InrcCoverRequirements.InrcDateSpecificCover.class)
    })
    protected List<Object> dayOfWeekCoverOrDateSpecificCover;

    /**
     * Gets the value of the dayOfWeekCoverOrDateSpecificCover property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the dayOfWeekCoverOrDateSpecificCover property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDayOfWeekCoverOrDateSpecificCover().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link InrcCoverRequirements.InrcDayOfWeekCover }
     * {@link InrcCoverRequirements.InrcDateSpecificCover }
     * 
     * 
     */
    public List<Object> getDayOfWeekCoverOrDateSpecificCover() {
        if (dayOfWeekCoverOrDateSpecificCover == null) {
            dayOfWeekCoverOrDateSpecificCover = new ArrayList<Object>();
        }
        return this.dayOfWeekCoverOrDateSpecificCover;
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
     *       &lt;sequence>
     *         &lt;element name="Date" type="{http://www.w3.org/2001/XMLSchema}date"/>
     *         &lt;element name="Cover" type="{}Cover" maxOccurs="unbounded"/>
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
        "date",
        "cover"
    })
    public static class InrcDateSpecificCover {

        @XmlElement(name = "Date", required = true)
        @XmlSchemaType(name = "date")
        protected XMLGregorianCalendar date;
        @XmlElement(name = "Cover", required = true)
        protected List<InrcCover> cover;

        /**
         * Gets the value of the date property.
         * 
         * @return
         *     possible object is
         *     {@link XMLGregorianCalendar }
         *     
         */
        public XMLGregorianCalendar getDate() {
            return date;
        }

        /**
         * Sets the value of the date property.
         * 
         * @param value
         *     allowed object is
         *     {@link XMLGregorianCalendar }
         *     
         */
        public void setDate(XMLGregorianCalendar value) {
            this.date = value;
        }

        /**
         * Gets the value of the cover property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the cover property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getCover().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link InrcCover }
         * 
         * 
         */
        public List<InrcCover> getCover() {
            if (cover == null) {
                cover = new ArrayList<InrcCover>();
            }
            return this.cover;
        }

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
     *       &lt;sequence>
     *         &lt;element name="Day" type="{}WeekDay"/>
     *         &lt;element name="Cover" type="{}Cover" maxOccurs="unbounded"/>
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
        "day",
        "cover"
    })
    public static class InrcDayOfWeekCover {

        @XmlElement(name = "Day", required = true)
        protected InrcWeekDay day;
        @XmlElement(name = "Cover", required = true)
        protected List<InrcCover> cover;

        /**
         * Gets the value of the day property.
         * 
         * @return
         *     possible object is
         *     {@link InrcWeekDay }
         *     
         */
        public InrcWeekDay getDay() {
            return day;
        }

        /**
         * Sets the value of the day property.
         * 
         * @param value
         *     allowed object is
         *     {@link InrcWeekDay }
         *     
         */
        public void setDay(InrcWeekDay value) {
            this.day = value;
        }

        /**
         * Gets the value of the cover property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the cover property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getCover().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link InrcCover }
         * 
         * 
         */
        public List<InrcCover> getCover() {
            if (cover == null) {
                cover = new ArrayList<InrcCover>();
            }
            return this.cover;
        }

    }

}
