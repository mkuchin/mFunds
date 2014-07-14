import org.jsoup.Jsoup

@Grab('org.jsoup:jsoup:1.7.3')

def pages = (1..89)
def baseUrl = "http://www.investsmart.com.au"
def blockSelector = { name, suffix = '' -> "div.panel h3:contains($name) + table $suffix" }

new File('data.txt').withPrintWriter { out ->
    pages.each { page ->
        println page
        try {
            def doc = Jsoup.connect("$baseUrl/ManagedFunds/Find?FundLegalType=1&MStarRating=0&OrderBy=4&OrderByOrientation=1&page=$page").get()
            doc.select('div.panel table.responsive tr:gt(0)').each {

                Thread.sleep(300)
                def href = it.select('td:eq(0) a').attr('href')

                def id = href.substring(href.lastIndexOf('/') + 1)
                if(!ids.contains(id)) {
                    println "Checking id=$id"
                    def localUrl = "/ManagedFunds/Fund/${id}"
                    def absUrl = new URL(new URL(baseUrl), localUrl).toString()
                    doc = Jsoup.connect(absUrl).get()
                    def general = doc.select(blockSelector('General Information'))
                    def name = general.select('tr:eq(0) td:eq(1)').text()
                    def sector = general.select('tr:eq(3) td:eq(1)').text()
                    def stars = general.select('tr:eq(4) td:eq(1) img').size()
                    def recommendation = general.select('tr:eq(5) td:eq(1)').text()
                    def spRating = general.select('tr:eq(6) td:eq(1)').text()
                    def founded = general.select('tr:eq(8) td:eq(1)').text()
                    def size = general.select('tr:eq(9) td:eq(1)').text().replaceAll('\\(.*\\)', '').replaceAll('million', '')
                    def performance = doc.select(blockSelector('Fund Performance', 'tr:eq(1)'))
                    def perf = []
                    (1..8).each { index ->
                        perf[index] = performance.select("td:eq($index)").text()
                    }

                    def offer = doc.select(blockSelector('Offer information'))
                    def initialSum = offer.select('tr td:contains(Minimum initial investment) + td').text()
                    def s = "$absUrl\t$name\t$sector\t$stars\t$recommendation\t$spRating\t$founded\t$size"
                    (1..8).each {
                        s += "\t${perf[it]}"
                    }
                    s += "\t${initialSum}"
                    out.println s
                }
            }
        } catch (e) {
            println e
        }
    }
}
