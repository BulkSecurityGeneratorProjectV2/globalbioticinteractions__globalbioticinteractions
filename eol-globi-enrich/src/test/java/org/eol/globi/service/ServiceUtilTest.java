package org.eol.globi.service;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class ServiceUtilTest {

    @Test
    public void extractElementValue() throws PropertyEnricherException {
        String response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><SOAP-ENV:Envelope SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\" xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:SOAP-ENC=\"http://schemas.xmlsoap.org/soap/encoding/\" xmlns:tns=\"http://aphia/v1.0\"><SOAP-ENV:Body><ns1:getAphiaRecordByIDResponse xmlns:ns1=\"http://tempuri.org/\"><return xsi:type=\"tns:AphiaRecord\"><AphiaID xsi:type=\"xsd:int\">729172</AphiaID><url xsi:type=\"xsd:string\">http://www.marinespecies.org/aphia.php?p=taxdetails&amp;id=729172</url><scientificname xsi:type=\"xsd:string\">Sterrhurus concavovesiculus</scientificname><authority xsi:type=\"xsd:string\">Reid, Coil &amp; Kuntz, 1965</authority><rank xsi:type=\"xsd:string\">Species</rank><status xsi:type=\"xsd:string\">unaccepted</status><unacceptreason xsi:type=\"xsd:string\">synonym</unacceptreason><valid_AphiaID xsi:type=\"xsd:int\">726834</valid_AphiaID><valid_name xsi:type=\"xsd:string\">Lecithochirium concavovesiculus</valid_name><valid_authority xsi:type=\"xsd:string\">(Reid, Coil &amp; Kuntz, 1965)</valid_authority><kingdom xsi:type=\"xsd:string\">Animalia</kingdom><phylum xsi:type=\"xsd:string\">Platyhelminthes</phylum><class xsi:type=\"xsd:string\">Trematoda</class><order xsi:type=\"xsd:string\">Plagiorchiida</order><family xsi:type=\"xsd:string\">Hemiuridae</family><genus xsi:type=\"xsd:string\">Sterrhurus</genus><citation xsi:type=\"xsd:string\">Gibson, D. (2014). Sterrhurus concavovesiculus Reid, Coil &amp; Kuntz, 1965. Accessed through:  World Register of Marine Species at http://www.marinespecies.org/aphia.php?p=taxdetails&amp;id=729172 on 2015-01-05</citation><lsid xsi:type=\"xsd:string\">urn:lsid:marinespecies.org:taxname:729172</lsid><isMarine xsi:type=\"xsd:int\">1</isMarine><isBrackish xsi:nil=\"true\" xsi:type=\"xsd:int\"/><isFreshwater xsi:nil=\"true\" xsi:type=\"xsd:int\"/><isTerrestrial xsi:nil=\"true\" xsi:type=\"xsd:int\"/><isExtinct xsi:nil=\"true\" xsi:type=\"xsd:int\"/><match_type xsi:type=\"xsd:string\">exact</match_type><modified xsi:type=\"xsd:string\">2013-09-27T07:20:03Z</modified></return></ns1:getAphiaRecordByIDResponse></SOAP-ENV:Body></SOAP-ENV:Envelope>";
        assertThat(ServiceUtil.extractName(response, "valid_AphiaID"), is("726834"));
        assertThat(ServiceUtil.extractName(response, "valid_name"), is("Lecithochirium concavovesiculus"));
    }

    @Test
    public void extractPath() throws PropertyEnricherException {
        String response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><SOAP-ENV:Envelope SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\" xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:SOAP-ENC=\"http://schemas.xmlsoap.org/soap/encoding/\" xmlns:tns=\"http://aphia/v1.0\"><SOAP-ENV:Body><ns1:getAphiaClassificationByIDResponse xmlns:ns1=\"http://tempuri.org/\"><return xsi:type=\"tns:Classification\"><AphiaID xsi:type=\"xsd:int\">1</AphiaID><rank xsi:type=\"xsd:string\">Superdomain</rank><scientificname xsi:type=\"xsd:string\">Biota</scientificname><child xsi:type=\"tns:Classification\"><AphiaID xsi:type=\"xsd:int\">2</AphiaID><rank xsi:type=\"xsd:string\">Kingdom</rank><scientificname xsi:type=\"xsd:string\">Animalia</scientificname><child xsi:type=\"tns:Classification\"><AphiaID xsi:type=\"xsd:int\">1821</AphiaID><rank xsi:type=\"xsd:string\">Phylum</rank><scientificname xsi:type=\"xsd:string\">Chordata</scientificname><child xsi:type=\"tns:Classification\"><AphiaID xsi:type=\"xsd:int\">146419</AphiaID><rank xsi:type=\"xsd:string\">Subphylum</rank><scientificname xsi:type=\"xsd:string\">Vertebrata</scientificname><child xsi:type=\"tns:Classification\"><AphiaID xsi:type=\"xsd:int\">1828</AphiaID><rank xsi:type=\"xsd:string\">Superclass</rank><scientificname xsi:type=\"xsd:string\">Gnathostomata</scientificname><child xsi:type=\"tns:Classification\"><AphiaID xsi:type=\"xsd:int\">11676</AphiaID><rank xsi:type=\"xsd:string\">Superclass</rank><scientificname xsi:type=\"xsd:string\">Pisces</scientificname><child xsi:type=\"tns:Classification\"><AphiaID xsi:type=\"xsd:int\">10194</AphiaID><rank xsi:type=\"xsd:string\">Class</rank><scientificname xsi:type=\"xsd:string\">Actinopterygii</scientificname><child xsi:type=\"tns:Classification\"><AphiaID xsi:type=\"xsd:int\">11014</AphiaID><rank xsi:type=\"xsd:string\">Order</rank><scientificname xsi:type=\"xsd:string\">Perciformes</scientificname><child xsi:type=\"tns:Classification\"><AphiaID xsi:type=\"xsd:int\">125567</AphiaID><rank xsi:type=\"xsd:string\">Family</rank><scientificname xsi:type=\"xsd:string\">Stromateidae</scientificname><child xsi:type=\"tns:Classification\"><AphiaID xsi:type=\"xsd:int\">159825</AphiaID><rank xsi:type=\"xsd:string\">Genus</rank><scientificname xsi:type=\"xsd:string\">Peprilus</scientificname><child xsi:type=\"tns:Classification\"><AphiaID xsi:type=\"xsd:int\">276560</AphiaID><rank xsi:type=\"xsd:string\">Species</rank><scientificname xsi:type=\"xsd:string\">Peprilus burti</scientificname><child xsi:type=\"tns:Classification\"></child></child></child></child></child></child></child></child></child></child></child></return></ns1:getAphiaClassificationByIDResponse></SOAP-ENV:Body></SOAP-ENV:Envelope>";
        assertThat(ServiceUtil.extractPath(response, "scientificname", ""), is("Biota | Animalia | Chordata | Vertebrata | Gnathostomata | Pisces | Actinopterygii | Perciformes | Stromateidae | Peprilus | Peprilus burti"));
        assertThat(ServiceUtil.extractPath(response, "rank", ""), is("Superdomain | Kingdom | Phylum | Subphylum | Superclass | Superclass | Class | Order | Family | Genus | Species"));
        assertThat(ServiceUtil.extractPath(response, "AphiaID", ""), is("1 | 2 | 1821 | 146419 | 1828 | 11676 | 10194 | 11014 | 125567 | 159825 | 276560"));
    }

}