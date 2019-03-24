package io.osguima3.jooqdsl.plugin.configuration

import org.jooq.util.jaxb.tools.StringAdapter
import java.io.Serializable
import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlType
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Container")
class Container : Serializable {

    @XmlElement(required = true)
    @XmlJavaTypeAdapter(StringAdapter::class)
    lateinit var provider: String

    @XmlElement(required = true)
    @XmlJavaTypeAdapter(StringAdapter::class)
    lateinit var version: String

    @XmlJavaTypeAdapter(StringAdapter::class)
    lateinit var migrationPath: String
}