package co.mesquita

import groovyx.net.http.HttpBuilder
import groovyx.net.http.optional.Download
import org.jsoup.Jsoup
import org.jsoup.nodes.*
import org.jsoup.select.Elements

import java.util.regex.Pattern

import static groovyx.net.http.HttpBuilder.*

class dataBot {

    // Obtem o html da pagina em formato de string
    static Document getUriPath(HttpBuilder http, String URL) {
        String htmlContent = http.get(){
            request.uri.path = URL
        }
        Document doc = Jsoup.parse(htmlContent)
        return doc
    }

    // Obtem o link contido no href da tag desejada
    static String getHrefContent(Document doc, Pattern pattern) {
        Elements links = doc.select('a[href]')
        def URL = ""
        for (Element link : links) {
            if (link.text() =~ pattern) {
                URL = link.attr("href")
                break
            }
        }

        URL = URL.split('gov.br')
        return URL[1]
    }

    static void main(String[] args) {

        // Define a URL base
        def http = configure {
            request.uri = 'https://www.gov.br'
        }

        final String ANS_URL = '/ans/pt-br'
        Document ans = getUriPath(http, ANS_URL)

        // Obtem a URL da pagina "Espaço do Prestador de Serviços de Saúde"
        def pattern = ~/Espa.o do Prestador de Servi.os de Sa.de/
        final String EPSS_URL = getHrefContent(ans, pattern)
        Document epss = getUriPath(http, EPSS_URL)

        // Obtem a URL da pagina "Troca de Informações na Saúde Suplementar"
        pattern = ~/TISS.*/
        final String TISS_URL = getHrefContent(epss, pattern)
        Document tiss = getUriPath(http, TISS_URL)

        // Obtem a URL da pagina "Padrão TISS - Versão Mês/Ano"
        pattern = ~/Clique aqui para acessar a vers.o.*/
        final String MES_TISS_URL = getHrefContent(tiss, pattern)
        Document mesTiss = getUriPath(http, MES_TISS_URL)

        // Obtem a URL da pagina "Padrão TISS – Histórico das versões"
        pattern = ~/Clique aqui para acessar todas as versões dos Componentes/
        final String HISTORICO_TISS_URL = getHrefContent(tiss, pattern)
        Document historicoTiss = getUriPath(http, HISTORICO_TISS_URL)

        // Obtem a URL da pagina "Padrão TISS – Tabelas Relacionadas"
        pattern = ~/Clique aqui para acessar as planilhas/
        final String TABELA_TISS_URL = getHrefContent(tiss, pattern)
        Document tabelaTiss = getUriPath(http, TABELA_TISS_URL)

        Elements links = mesTiss.select('a[href]')

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

        Element table = historicoTiss.select("table").get(0)
        Elements rows = table.select("tr")

        for (Element row : rows) {
            if (row.text().find('jan/2016')) break
            println row.text()
        }

        links = tabelaTiss.select('a[href]')

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