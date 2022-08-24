package com.expleo.mergereport;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.text.WordUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * Hello world!
 *
 */
public class App {

        public static void main(String[] args) throws IOException, URISyntaxException, ParseException {
                Options options = new Options();
                options.addOption("i", "input", true, "input file path");
                options.addOption("o", "output", true, "output file name");
                options.addOption("r", "overall", true, "overall results file");
                options.addOption("w", "week", true, "week number");
                options.addOption("d", "date", true, "date");
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("help", options);
                CommandLineParser parser = new DefaultParser();
                CommandLine cmd = parser.parse(options, args);
                String inputFilePath = cmd.getOptionValue("input");
                String outputFileName = cmd.getOptionValue("output");
                String overallDataFile = cmd.getOptionValue("overall");

                StringBuilder finalHtml = new StringBuilder();
                new File(inputFilePath + "\\" + outputFileName + ".html").delete();
                InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("1.head.txt");
                BufferedReader br = new BufferedReader(new InputStreamReader(stream));

                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                        sb.append(line + System.lineSeparator());
                }

                finalHtml.append(sb);

                stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("2.body_till_metrics.txt");
                br = new BufferedReader(new InputStreamReader(stream));

                sb = new StringBuilder();
                while ((line = br.readLine()) != null) {
                        sb.append(line + System.lineSeparator());
                }

                finalHtml.append(sb);

                List<Path> files = Files.list(Paths.get(inputFilePath)).filter(Files::isRegularFile)
                                .filter(path -> path.toString().endsWith(".html")).collect(Collectors.toList());
                List<HashMap<String, String>> alldata = new ArrayList<>();

                for (Path temp : files) {
                        System.out.println("Processing File : " + temp.getFileName().toString());
                        String filename = temp.getFileName().toString().toLowerCase();
                        String[] sp = filename.substring(filename.indexOf("_") + 1, filename.lastIndexOf("_"))
                                        .split(" ");
                        HashMap<String, String> data = new HashMap<>();
                        data.put("platform", "Windows");
                        data.put("browser", "Chrome");
                        String platform = sp[0];
                        String browser = sp[1];
                        data.put("platform", WordUtils.capitalize(platform));
                        data.put("browser", WordUtils.capitalize(browser));
                        data.put("platform_logo",
                                        (platform.contains("mac") || platform.contains("ios")) ? "fab fa-apple"
                                                        : (platform.contains("android") || platform.contains("mobile"))
                                                                        ? "fas fa-mobile"
                                                                        : "fab fa-windows");
                        data.put("browser_logo", browser.toLowerCase());

                        Document html = Jsoup.parse(new String(Files.readAllBytes(temp)));

                        data.put("no_features", html.select("#dashboard-view > div > div:eq(1) > div:eq(0) > div > div")
                                        .text());
                        data.put("no_tests", html.select("#dashboard-view > div > div:eq(1) > div:eq(1) > div > div")
                                        .text());
                        data.put("no_steps", html.select("#dashboard-view > div > div:eq(1) > div:eq(2) > div > div")
                                        .text());
                        data.put("run_start_time", html
                                        .select("#dashboard-view > div > div:eq(1) > div:eq(3) > div > div").text());
                        data.put("run_end_time", html
                                        .select("#dashboard-view > div > div:eq(1) > div:eq(4) > div > div").text());
                        data.put("total_time", html.select("#dashboard-view > div > div:eq(1) > div:eq(5) > div > div")
                                        .text());
                        data.put("features_passed",
                                        html.select("#charts-row > div:eq(0) > div > div:eq(2) > span > span").text());
                        data.put("features_failed",
                                        html.select("#charts-row > div:eq(0) > div > div:eq(3) > span:eq(0)").text());
                        data.put("scenario_passed",
                                        html.select("#charts-row > div:eq(1) > div > div:eq(2) > span > span").text());
                        data.put("scenario_failed",
                                        html.select("#charts-row > div:eq(1) > div > div:eq(3) > span:eq(0)").text());
                        data.put("steps_passed",
                                        html.select("#charts-row > div:eq(2) > div > div:eq(2) > span > span").text());
                        data.put("steps_failed",
                                        html.select("#charts-row > div:eq(2) > div > div:eq(3) > span:eq(0)").text());
                        alldata.add(data);
                }

                String metrics = "<tr> \n" + "        <th>Platform &nbsp;&nbsp;<i class=\"PlatformLogo\"></i></th>\n"
                                + "        <th>Browser &nbsp;&nbsp; <i class=\"fab fa-BrowserLogo\"></i></th>\n"
                                + "        <th>Test Scenario</th>\n" + "        <th>Test Steps</th>\n"
                                + "        <td>\n" + "            <span class=\"text-success\">\n"
                                + "                <i class=\"fas fa-caret-up me-1\"></i>\n"
                                + "                <span>No of Scenario's Passed</span>\n" + "            </span>\n"
                                + "        </td>\n" + "        <td>\n" + "            <span class=\"text-danger\">\n"
                                + "                <i class=\"fas fa-caret-down me-1\"></i>\n"
                                + "                <span>No of Scenario's Failed</span>\n" + "            </span>\n"
                                + "        </td>\n" + "        <td>\n" + "            <span class=\"text-success\">\n"
                                + "                <i class=\"fas fa-caret-up me-1\"></i>\n"
                                + "                <span>pass_percentage%</span>\n" + "            </span>\n"
                                + "        </td>\n" + "        <td>\n" + "            <span class=\"text-danger\">\n"
                                + "                <i class=\"fas fa-caret-down me-1\"></i>\n"
                                + "                <span>fail_percentage%</span>\n" + "            </span>\n"
                                + "        </td>\n" + "    </tr>\n";

                for (HashMap<String, String> temp : alldata) {
                        finalHtml.append(metrics.replace("PlatformLogo", temp.get("platform_logo"))
                                        .replace("Platform", temp.get("platform"))
                                        .replace("BrowserLogo", temp.get("browser_logo"))
                                        .replace("Browser", temp.get("browser"))
                                        .replace("Test Scenario", temp.get("no_tests"))
                                        .replace("Test Steps", temp.get("no_steps"))
                                        .replace("No of Scenario's Passed", temp.get("scenario_passed"))
                                        .replace("No of Scenario's Failed", temp.get("scenario_failed"))
                                        .replace("pass_percentage%", ((Float
                                                        .parseFloat(temp.get("scenario_passed").replace(",", ""))
                                                        / Float.parseFloat(temp.get("no_tests").replace(",", "")))
                                                        * 100) + "%")
                                        .replace("fail_percentage%", ((Float
                                                        .parseFloat(temp.get("scenario_failed").replace(",", ""))
                                                        / Float.parseFloat(temp.get("no_tests").replace(",", "")))
                                                        * 100) + "%"));
                }

                finalHtml.append("'</tbody> \n </table>\n </div>\n </div>\n </div>\n'</section>'");

                String overall = "  <span class=\"anchor\" id=\"OverallReport\"></span> " + "<section class=\"mb-4\" >"
                                + "                    <div class=\"card\">"
                                + "                        <div class=\"card-header py-3\">"
                                + "                            <h5 class=\"mb-0 text-center\">"
                                + "                                <strong>Overall - Weekly Result </strong>"
                                + "                            </h5>" + "                        </div>"
                                + "                        <div class=\"row\">"
                                + "                            <div class=\"col-sm\">"
                                + "                                <canvas id=\"overallchart\" height=\"100\"></canvas>"
                                + "                            </div>     </div>" + "                        </div>"
                                + "                </section><span class=\"anchor\" id=\"Weekly\"></span>";

                finalHtml.append(overall);

                String weekly = "  <section class=\"mb-4\" >" + "                    <div class=\"card\">"
                                + "                        <div class=\"card-header py-3\">"
                                + "                            <h5 class=\"mb-0 text-right\">"
                                + "                                <strong>Platform - Browser Split Up's </strong>"
                                + "							&nbsp;&nbsp;<i class=\"PlatformLogo\"></i>"
                                + "							&nbsp;&nbsp;<i class=\"fab fa-BrowserLogo\"></i>"
                                + "                            </h5>" + "                        </div>"
                                + "                        <div class=\"row\">"
                                + "                            <div class=\"col-sm\">"
                                + "                                <canvas id=\"PlatformBrowserFeature\" height=\"100\"></canvas>"
                                + "                            </div>"
                                + "                            <div class=\"col-sm\">"
                                + "                                <canvas id=\"PlatformBrowserTest\" height=\"100\"></canvas>"
                                + "                            </div>"
                                + "                            <div class=\"col-sm\">"
                                + "                                <canvas id=\"PlatformBrowserSteps\"></canvas>"
                                + "                            </div>" + "                        </div>"
                                + "                        <div class=\"row\">"
                                + "                            <div class=\"col-sm\">"
                                + "                                <h5 class=\"mb-0 text-Right\">"
                                + "								&nbsp;&nbsp;<i class=\"fas fa-list-ol #ac00e6\"></i>"
                                + "                                    <strong>Features</strong><br>"
                                + "                                </h5><h6  class=\"center\">"
                                + "                                    no_features"
                                + "                                </h6>" + "                            </div>"
                                + "                            <div class=\"col-sm\">"
                                + "                                <h5 class=\"mb-0 text-Right\">"
                                + "								&nbsp;&nbsp;<i class=\"fas fa-list-ol #ac00e6\"></i>"
                                + "                                    <strong>No. of Tests</strong><br>"
                                + "                                </h5><h6  class=\"center\">"
                                + "                                    no_tests"
                                + "                                </h6>" + "                            </div>"
                                + "                            <div class=\"col-sm\">"
                                + "                                <h5 class=\"mb-0 text-Right\">"
                                + "								<i class=\"fas fa-list-ol \"></i>"
                                + "                                    <strong>No. of Steps</strong><br>"
                                + "                                </h5><h6  class=\"center\">"
                                + "                                    no_steps"
                                + "                                </h6>" + "                            </div>"
                                + "                            <div class=\"col-sm\">"
                                + "                                <h5 class=\"mb-0 text-Right\">"
                                + "								<i class=\"fas fa-stopwatch\"></i>"
                                + "                                    <strong>Run Start Time</strong><br>"
                                + "                                </h5><h6  class=\"center\">"
                                + "                                    run_start_time"
                                + "                                </h6>" + "                            </div>"
                                + "                            <div class=\"col-sm\">"
                                + "                                <h5 class=\"mb-0 text-Right\">"
                                + "								<i class=\"fas fa-stopwatch\"></i>"
                                + "                                    <strong>End Time</strong><br>"
                                + "                                </h5><h6  class=\"center\">"
                                + "                                    run_end_time"
                                + "                                </h6>" + "                            </div>"
                                + "                            <div class=\"col-sm\">"
                                + "                                <h5 class=\"mb-0 text-Right\">"
                                + "								<i class=\"fas fa-business-time\"></i>"
                                + "                                    <strong>Time Taken</strong><br>"
                                + "                                </h5><h6  class=\"center\">"
                                + "                                    total_time"
                                + "                                </h6>" + "                            </div>"
                                + "                        </div>" + "                    </div>"
                                + "                </section>";

                for (HashMap<String, String> temp : alldata) {
                        finalHtml.append(weekly.replace("PlatformLogo", temp.get("platform_logo"))
                                        .replace("Platform", temp.get("platform"))
                                        .replace("BrowserLogo", temp.get("browser_logo"))
                                        .replace("Browser", temp.get("browser"))
                                        .replace("no_features", temp.get("no_features"))
                                        .replace("no_tests", temp.get("no_tests"))
                                        .replace("no_steps", temp.get("no_steps"))
                                        .replace("run_start_time", temp.get("run_start_time"))
                                        .replace("run_end_time", temp.get("run_end_time"))
                                        .replace("total_time", temp.get("total_time")));
                }
                finalHtml.append(
                                " </div> \n </main> \n <!--Main layout--> \n <script src=\"https://cdn.jsdelivr.net/npm/chart.js\"></script>");

                String chart = "<script>"
                                + "            var ctx = document.getElementById(\"Overall\").getContext(\"2d\");"
                                + "            var Overall = new Chart(ctx, {" + "                type: \"pie\","
                                + "                data: {" + "                    labels: [\"Pass%\", \"Fail%\"],"
                                + "                    datasets: [" + "                        {"
                                + "                            label: \"My First Dataset\","
                                + "                            data: [cumulative_results],"
                                + "                            backgroundColor: [\"#ac00e6\", \"grey\"],"
                                + "                            hoverOffset: 4," + "                        },"
                                + "                    ]," + "                }," + "                options: {"
                                + "                    maintainAspectRatio: false," + "                    plugins: {"
                                + "                        title: {" + "                            display: true,"
                                + "                            text: \"Cumulative Results\","
                                + "                        }," + "                    }," + "                },"
                                + "            });"
                                + "            var ctx = document.getElementById(\"Device\").getContext(\"2d\");"
                                + "            var data = {" + "                labels: [device_label],"
                                + "                datasets: [" + "                    {"
                                + "                        label: \"Pass\","
                                + "                        backgroundColor: \"#ac00e6\","
                                + "                        data: [device_pass]," + "                    },"
                                + "                    {" + "                        label: \"Fail\","
                                + "                        backgroundColor: \"grey\","
                                + "                        data: [device_fail]," + "                    },"
                                + "                ]," + "            };" + "            var Device = new Chart(ctx, {"
                                + "                type: \"bar\"," + "                data: data,"
                                + "                options: {" + "                    barValueSpacing: 20,"
                                + "                    plugins: {" + "                        title: {"
                                + "                            display: true,"
                                + "                            text: \"Device Split Up\","
                                + "                        }," + "                    },"
                                + "                    scales: {" + "                        yAxes: ["
                                + "                            {" + "                                ticks: {"
                                + "                                    min: 0," + "                                },"
                                + "                            }," + "                        ],"
                                + "                    }," + "                }," + "            });";

                Float passed = 0.0f;
                Float failed = 0.0f;
                StringBuilder d_label = new StringBuilder();
                StringBuilder d_pass = new StringBuilder();
                StringBuilder d_fai = new StringBuilder();
                for (HashMap<String, String> temp : alldata) {
                        d_label.append("\"").append(temp.get("platform")).append(" ").append(temp.get("browser"))
                                        .append("\"").append(",");
                        d_pass.append(temp.get("scenario_passed")).append(",");
                        d_fai.append(temp.get("scenario_failed")).append(",");
                        passed = passed + Float.parseFloat(temp.get("scenario_passed"));
                        failed = failed + Float.parseFloat(temp.get("scenario_failed"));

                }
                String cumulative_results = (passed / (passed + failed) * 100) + ","
                                + (failed / (passed + failed) * 100);
                String device_label = d_label.toString().replaceAll(",$", "");
                String device_pass = d_pass.toString().replaceAll(",$", "");
                String device_fail = d_fai.toString().replaceAll(",$", "");

                finalHtml.append(chart.replace("cumulative_results", cumulative_results)
                                .replace("device_label", device_label).replace("device_pass", device_pass)
                                .replace("device_fail", device_fail));

                String overallchart = "var ctx = document.getElementById(\"overallchart\").getContext(\"2d\");"
                                + "            var data = {" + "                labels: [Overall_label],"
                                + "                datasets: [" + "                    {"
                                + "                        label: \"Pass\","
                                + "                        backgroundColor: \"#ac00e6\","
                                + "                        data: [Overall_pass]," + "                    },"
                                + "                    {" + "                        label: \"Fail\","
                                + "                        backgroundColor: \"grey\","
                                + "                        data: [Overall_fail]," + "                    },"
                                + "                ]," + "            };" + "            var Device = new Chart(ctx, {"
                                + "                type: \"bar\"," + "                data: data,"
                                + "                options: {" + "                    barValueSpacing: 20,"
                                + "                    plugins: {" + "                        title: {"
                                + "                            display: true,"
                                + "                            text: \"Overall Weekly\"," + "                        },"
                                + "                    }," + "                    scales: {"
                                + "                        yAxes: [" + "                            {"
                                + "                                ticks: {"
                                + "                                    min: 0," + "                                },"
                                + "                            }," + "                        ],"
                                + "                    }," + "                }," + "            });";

                List<String> allLines = Files.readAllLines(Paths.get(overallDataFile));
                allLines.remove(0);
                StringBuilder o_label = new StringBuilder();
                StringBuilder o_pass = new StringBuilder();
                StringBuilder o_fail = new StringBuilder();
                String lastweek = null;
                for (String weeek_line : allLines) {
                        String[] data = weeek_line.split("&");
                        lastweek = data[0];
                        o_label.append("\"").append(data[0]).append("\"").append(",");
                        o_pass.append(Float.parseFloat(data[5].replace(",", ".")) * 100).append(",");
                        o_fail.append(Float.parseFloat(data[6].replace(",", ".")) * 100).append(",");

                }
                String overall_label = o_label.toString().replaceAll(",$", "");
                String overall_pass = o_pass.toString().replaceAll(",$", "");
                String overall_fail = o_fail.toString().replaceAll(",$", "");
                finalHtml.append(overallchart.replace("Overall_label", overall_label)
                                .replace("Overall_pass", overall_pass).replace("Overall_fail", overall_fail));

                if (!lastweek.equals(cmd.getOptionValue("week"))) {
                        FileWriter fw = new FileWriter(overallDataFile, true);
                        BufferedWriter bw = new BufferedWriter(fw);
                        bw.newLine();
                        bw.write(cmd.getOptionValue("week")+"&"+cmd.getOptionValue("date")+"&"+(Math.round(passed) + Math.round(failed))+"&"+Math.round(passed)+"&"+Math.round(failed)+"&"+(passed / (passed + failed))+"&"+(failed / (passed + failed)));
                        bw.close();
                }

                
                String splitup = "var ctx = document.getElementById(\"PlatformBrowserFeature\").getContext(\"2d\");"
                                + "            var ChromeTest = new Chart(ctx, {"
                                + "                type: \"doughnut\"," + "                data: {"
                                + "                    labels: [\"Pass\", \"Fail\"],"
                                + "                    datasets: [" + "                        {"
                                + "                            data: [features_passed, features_failed],"
                                + "                            backgroundColor: [\"#ac00e6\", \"grey\"],"
                                + "                            hoverOffset: 4," + "                        },"
                                + "                    ]," + "                }," + "                options: {"
                                + "                    maintainAspectRatio: false," + "                    plugins: {"
                                + "                        title: {" + "                            display: true,"
                                + "                            text: \"Features\"," + "                        },"
                                + "                    }," + "                }," + "            });"
                                + "var ctx = document.getElementById(\"PlatformBrowserTest\").getContext(\"2d\");"
                                + "            var ChromeTest = new Chart(ctx, {"
                                + "                type: \"doughnut\"," + "                data: {"
                                + "                    labels: [\"Pass\", \"Fail\"],"
                                + "                    datasets: [" + "                        {"
                                + "                            data: [scenario_passed, scenario_failed],"
                                + "                            backgroundColor: [\"#ac00e6\", \"grey\"],"
                                + "                            hoverOffset: 4," + "                        },"
                                + "                    ]," + "                }," + "                options: {"
                                + "                    maintainAspectRatio: false," + "                    plugins: {"
                                + "                        title: {" + "                            display: true,"
                                + "                            text: \"Tests\"," + "                        },"
                                + "                    }," + "                }," + "            });"
                                + "            var ctx = document.getElementById(\"PlatformBrowserSteps\").getContext(\"2d\");"
                                + "            var ChromeTest = new Chart(ctx, {"
                                + "                type: \"doughnut\"," + "                data: {"
                                + "                    labels: [\"Pass\", \"Fail\"],"
                                + "                    datasets: [" + "                        {"
                                + "                            data: [steps_passed, steps_failed],"
                                + "                            backgroundColor: [\"#ac00e6\", \"grey\"],"
                                + "                            hoverOffset: 4," + "                        },"
                                + "                    ]," + "                }," + "                options: {"
                                + "                    maintainAspectRatio: false," + "                    plugins: {"
                                + "                        title: {" + "                            display: true,"
                                + "                            text: \"Steps\"," + "                        },"
                                + "                    }," + "                }," + "            });";

                for (HashMap<String, String> temp : alldata) {
                        finalHtml.append(splitup.replace("Platform", temp.get("platform"))
                                        .replace("Browser", temp.get("browser"))
                                        .replace("features_passed", temp.get("features_passed").replace(",", ""))
                                        .replace("features_failed", temp.get("features_failed").replace(",", ""))
                                        .replace("scenario_passed", temp.get("scenario_passed").replace(",", ""))
                                        .replace("scenario_failed", temp.get("scenario_failed").replace(",", ""))
                                        .replace("steps_passed", temp.get("steps_passed").replace(",", ""))
                                        .replace("steps_failed", temp.get("steps_failed").replace(",", "")));
                }

                finalHtml.append(" </script> \n                </body> \n             </html>");

                Files.write(Paths.get(inputFilePath + "\\" + outputFileName + ".html"),
                                Jsoup.parse(finalHtml.toString()).toString().getBytes());
        }
}
