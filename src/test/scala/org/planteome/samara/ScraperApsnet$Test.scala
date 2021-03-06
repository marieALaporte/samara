package org.planteome.samara

import org.scalatest._


class ScraperApsnet$Test extends FlatSpec with Matchers {

  "scraper" should "produce a giant list of disease interactions" in {
    val diseases = ScraperApsnet.scrapeDiseases().map(_._3)

//    diseases should contain(Disease(name = "Bacterial canker",
//      verbatimPathogen = "Pseudomonas syringae pv. syringae van Hall 1902",
//      pathogen = "Pseudomonas syringae pv. syringae van Hall 1902",
//      //pathogenId = "no:match",
//      pathogenId = "NCBITaxon:205918",
//      verbatimHost = expectedHosts.verbatimPeachHost,
//      host = "Prunus persica",
//      hostId = "NCBITaxon:3760",
//      citation = expectedCitations.peachAndNectarine))
//
//    diseases should contain(Disease(name = "Bacterial canker",
//      verbatimPathogen = "Pseudomonas syringae pv. syringae van Hall 1902",
//      pathogen = "Pseudomonas syringae pv. syringae van Hall 1902",
//      pathogenId = "NCBITaxon:205918",
//      verbatimHost = expectedHosts.verbatimPeachHost,
//      host = "Prunus persica var. nucipersica",
//      hostId = "NCBITaxon:3760",
//      //hostId = "NCBITaxon:323851",
//      citation = expectedCitations.peachAndNectarine))
//
//    diseases should contain(Disease(name = "Green fruit rot",
//      verbatimPathogen = "M. laxa (Aderhold & Ruhland) Honey",
//      pathogen = "Monilinia laxa (Aderhold & Ruhland) Honey",
//      pathogenId = "NCBITaxon:61186",
//      verbatimHost = expectedHosts.verbatimPeachHost,
//      host = "Prunus persica",
//      hostId = "NCBITaxon:3760",
//      citation = expectedCitations.peachAndNectarine))
//
//
//    diseases should contain(Disease(name = "Alfalfa witches’-broom",
//      verbatimPathogen = "‘Candidatus Phytoplasma asteris’",
//      pathogen = "Candidatus Phytoplasma asteris",
//      pathogenId = "NCBITaxon:1414854",
//      verbatimHost = "Diseases of Alfalfa (Medicago sativa L.)",
//      host = "Medicago sativa L.",
//      hostId = "NCBITaxon:36902",
//      //hostId = "NCBITaxon:3879",
//      citation = expectedCitations.alfalfa))

    diseases should not be empty

    println(s"found ${diseases.size} disease interactions:")

  }

}