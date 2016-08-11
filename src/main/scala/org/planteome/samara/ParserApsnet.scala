package org.planteome.samara

import java.io.File

import net.ruippeixotog.scalascraper.model.Document
import net.ruippeixotog.scalascraper.scraper.ContentExtractors._

import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import org.globalnames.parser.ScientificNameParser.{instance => snp}


import scala.io.Source


// http://www.apsnet.org/publications/commonnames/Pages/default.aspx

case class Disease(name: String,
                   verbatimPathogen: String = "", pathogen: String,
                   verbatimHost: String = "", host: String, citation: String = "")

abstract class ParserApsnet extends NameFinder with Scrubber {

  def parsePageIndex(doc: Document): Iterable[String] = {
    doc >> elements(".link-item") >> attrs("href")("a")
  }

  def parse(doc: Document): Iterable[Disease] = {
    val diseaseLists = doc >> elements("dt,dd")
    val elems = diseaseLists.map(elem => {
      (elem.tagName, elem >> text(elem.tagName))
    })

    val withDisease = elems
      .zipWithIndex
      .foldLeft(List.empty[String])((acc, item) => {
        item._1 match {
          case ("dt", v) => v :: acc
          case ("dd", _) => acc.head :: acc
        }
      }).reverse

    val targetTaxonNames = doc >> text("h1")
    val authorInfo = doc >> text("h4")

    val citation = s"$authorInfo. $targetTaxonNames. The American Phytopathological Society."

    val diseases = findNames(targetTaxonNames)
      .flatMap(targetTaxon => {
        elems.zip(withDisease)
          .filter {
            case (("dd", _), _) => true
            case _ => false
          }
          .flatMap {
            case ((_, pathogenName), diseaseName) => {
              val diseases = Seq(Disease(name = scrub(diseaseName), pathogen = pathogenName, host = targetTaxon))

              diseases.flatMap {
                disease => {
                  val hostNames: Seq[String] = extractHostNames(targetTaxon)
                  hostNames.map { hostname => disease.copy(host = hostname, verbatimHost = targetTaxon) }
                }
              }.flatMap {
                disease => {
                  val pathogenNames: Seq[String] = extractPathogenNames(pathogenName)
                  pathogenNames.map { pathogen => disease.copy(pathogen = pathogen, verbatimPathogen = pathogenName) }
                }
              }
            }
          }
      })

    val expandedDiseases = diseases zip expandPrefixes(diseases.map(_.pathogen)) map {
      case ((disease, pathogenExpanded)) => disease.copy(pathogen = pathogenExpanded, citation = citation)
    }

    expandedDiseases.map {
      disease => disease.copy(pathogen = canonize(disease.pathogen), host = canonize(disease.host))
    }
  }

  def canonize(name: String): String = {
    val capital = """(^[A-Z].*)""".r
    name match {
      case capital(capitalized) => name
      case _ => ""
    }
  }

  def expandPrefixes(names: List[String]): List[String] = {
    names.foldLeft((names.headOption.getOrElse(""), List[String]())) { (acc, name) =>
      val abbreviated = """(^\w[\.]*)\s+.*""".r
      name match {
        case abbreviated(abbr) => (acc._1, name.replaceFirst(abbr, acc._1) :: acc._2)
        case str => (str.split("\\s").head, name :: acc._2)
      }
    }._2.reverse
  }

  lazy val nameMap: Map[String, String] = Source.fromInputStream(getClass.getResourceAsStream("apsnetNameMap.tsv"))
    .getLines()
    .foldLeft(Map[String, String]()) {
      (agg, line) =>
        val split = line.split('\t')
        agg ++ Map(split(0) -> split(1))
    }

  def extractHostNames(targetTaxon: String): Seq[String] = {
    val scrubbedHost: String = scrub(targetTaxon)

    nameMap.get(scrubbedHost) match {
      case Some(name) => {
        name.split('|')
      }
      case _ => singleHostname(scrubbedHost)
    }
  }

  def extractPathogenNames(sourceTaxon: String): Seq[String] = {
    val scrubbedName: String = scrub(sourceTaxon)
    val names: Seq[String] = pathogenNames(scrubbedName)
    names.flatMap { someName => nameMap.get(someName) match {
      case Some(name) => name.split('|')
      case None => Seq(someName)
    }
    }
  }

  def pathogenNames(pathogenName2: String): Seq[String] = {

    val synonymSplit = """^\(=(.*)\)$""".r
    val synonymSplit3 = """^\(=(.*)$""".r
    val synonymSplit2 = """^=(.*)""".r
    val synonymSplit4 = """\(syn\.(.*)\)$""".r
    val synonymSplit5 = """\(syns\.(.*)\)$""".r
    val anamorph = """^\((.*morph):(.*)?\)$""".r
    val pathogenName = pathogenName2 match {
      case synonymSplit(name) => name.trim
      case synonymSplit2(name) => name.trim
      case synonymSplit3(name) => name.trim
      case synonymSplit5(name) => name.trim
      case synonymSplit4(name) => name.trim
      case anamorph(_, name) => name.trim
      case _ => pathogenName2
    }

    val genusSpeciesSplit = """[Gg]enus\s([^;:,\s])*(.*)""".r
    val removeParenthesis: String = pathogenName
      .replaceAll("""\s+""", " ")
      .replaceAll("""\([^\)]*\)""", ",")
    val pathogenNameProcessed = removeParenthesis match {
      case genusSpeciesSplit(genusName, postGenusName) => {
        postGenusName
      }
      case _ => pathogenName
    }

    pathogenNameProcessed.split("""[,:;]""")
      .map(_.trim)
      .filter(_.nonEmpty)
  }

  def singleHostname(scrubbedHost: String): Seq[String] = {
    val singleName = """[^:]*\((.*)\).*""".r
    Seq(scrubbedHost match {
      case singleName(name) => name
      case _ => scrubbedHost
    })
  }
}
