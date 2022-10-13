package co.mesquita

import groovyx.net.http.HttpBuilder
import groovyx.net.http.optional.Download
import org.jsoup.Jsoup
import org.jsoup.nodes.*
import org.jsoup.select.Elements

import static groovyx.net.http.HttpBuilder.*

class dataBot {
    static void main(String[] args) {

        // Define a URL base
        def http = configure {
            request.uri = 'https://www.gov.br'
        }

        // obtem o html da pagina em formato de string
        String ans = http.get(){
            request.uri.path = '/ans/pt-br'
        }

        // transforma a string em document
        Document doc = Jsoup.parse(ans)
        Elements links = doc.select('a[href]')

        // Verifica a ocorrencia do menu desejado e entao
        // captura a URL vinculada a tag
        def pattern = ~/Espa.o do Prestador de Servi.os de Sa.de/
        def prestadoresURL = ""
        for (Element link : links) {
            if (link.text() =~ pattern) {
                prestadoresURL = link.attr("href")
                break
            }
        }

        // Utitliza a URL encontrada para reproduzir as etapas
        // anteriores e atingir o objetivo
        prestadoresURL = prestadoresURL.split('gov.br')
        String epss = http.get(){
            request.uri.path = prestadoresURL[1]
        }

        doc = Jsoup.parse(epss)
        links = doc.select('a[href]')

        pattern = ~/TISS.*/
        def tissURL = ""
        for (Element link : links) {
            if (link.text() =~ pattern) {
                tissURL = link.attr("href")
                break
            }
        }

        // Repetimos o processo para a pagina TISS
        tissURL = tissURL.split('gov.br')
        String tiss = http.get(){
            request.uri.path = tissURL[1]
        }

        doc = Jsoup.parse(tiss)
        links = doc.select('a[href]')

        pattern = ~/Clique aqui para acessar a vers.o.*/
        def mesTISS_URL = ""
        for (Element link : links) {
            if (link.text() =~ pattern) {
                mesTISS_URL = link.attr("href")
                break
            }
        }

        mesTISS_URL = mesTISS_URL.split('gov.br')
        String mtiss = http.get(){
            request.uri.path = mesTISS_URL[1]
        }

        doc = Jsoup.parse(mtiss)
        links = doc.select('a[href]')

        pattern = ~/Componente de Comunica..o/
        def compC_URL = ""
        for (Element link : links) {
            if (link.text() =~ pattern) {
                compC_URL = link.attr("href")
                break
            }
        }

        File file = HttpBuilder.configure {
            request.uri = compC_URL
        }.get {
            Download.toFile(delegate, new File('downloads/PadroTISSComunicacao.zip'))
        }

        doc = Jsoup.parse(tiss)
        links = doc.select('a[href]')

        pattern = ~/Clique aqui para acessar todas as versões dos Componentes/
        def historicoTISS_URL = ""
        for (Element link : links) {
            if (link.text() =~ pattern) {
                historicoTISS_URL = link.attr("href")
                break
            }
        }

        historicoTISS_URL = historicoTISS_URL.split('gov.br')

        String htiss = http.get(){
            request.uri.path = historicoTISS_URL[1]
        }

        doc = Jsoup.parse(htiss)
        Element table = doc.select("table").get(0)
        Elements rows = table.select("tr")

        for (Element row : rows) {
            if (row.text().find('jan/2016')) break
            println row.text()
        }

        doc = Jsoup.parse(tiss)
        links = doc.select('a[href]')

        pattern = ~/Clique aqui para acessar as planilhas/
        def tabelasTISS_URL = ""
        for (Element link : links) {
            if (link.text() =~ pattern) {
                tabelasTISS_URL = link.attr("href")
                break
            }
        }

        tabelasTISS_URL = tabelasTISS_URL.split('gov.br')

        String tatiss = http.get(){
            request.uri.path = tabelasTISS_URL[1]
        }

        doc = Jsoup.parse(tatiss)
        links = doc.select('a[href]')

        pattern = ~/Clique aqui para baixar a tabela de erros no envio para a ANS.*/
        def tableE_URL = ""
        for (Element link : links) {
            if (link.text() =~ pattern) {
                tableE_URL = link.attr("href")
                break
            }
        }

        file = HttpBuilder.configure {
            request.uri = tableE_URL
        }.get {
            Download.toFile(delegate, new File('downloads/tabela-erros-envio-para-ans-padrao.xlsx'))
        }
    }
}