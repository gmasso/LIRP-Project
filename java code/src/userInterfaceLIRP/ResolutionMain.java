package userInterfaceLIRP;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import ilog.concert.IloException;
import instanceManager.Instance;
import solverLIRP.LocManager;
import solverLIRP.Matheuristics;
import solverLIRP.RouteManager;
import solverLIRP.Solution;
import tools.JSONParser;

public class ResolutionMain {
	/**
	 * @param args
	 * @throws IOException
	 * @throws IloException
	 */

	public static void main(String[] args) throws IOException, IloException {	
		/*********************
		 *     PARAMETERS     
		 *********************/

		/* Number of routes in each subset when separating */
		int[] splitParam = {0, 0}; // Size of the subsets of routes
		boolean[] withLoops = {false, true};
		//splitParam = 0; // Uncomment to solve the original instance without sampling the routes

		String instDir = "../Instances/Complete/Small/";

		/* Get all the files in the directory */
		File listInst = new File(instDir);
		try {
			/* Loop through all the files in the instances directory */
			for (String fileName : listInst.list() ) {
				String fileNameSol = "../Solutions/" + fileName.replace(".json", "_sol.json");
				if(splitParam[0] > 0) {
					fileNameSol = "../Solutions/" + fileName.replace(".json", "_split"+ splitParam + "_sol.json");
				}
				File fileSol = new File(fileNameSol);

				String extension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
				if(extension!= null && extension.equals("json") && fileName.contains("dc0-10r-")) {
					/* Create the instance from the json file */
					Instance instLIRP = new Instance(instDir + fileName);
					System.out.print("Solving instance " + fileName + "...");

					/* Create the log file and solution file to store the results and the trace of the program */
					String fileNameLog = "../Log files/" + fileName.replace(".json", "_log.json");
					File fileLog = new File(fileNameLog);
					PrintStream printStreamLog = new PrintStream(fileLog);
					
					/* Outputs out and err are redirected to the log file */
					PrintStream original = System.out;

					System.out.print("Creating the RouteManager...");
					RouteManager rm = new RouteManager(instLIRP);
					rm.initialize(false);
					rm.writeToJSONFile("../Log files/" + fileName.replace(".json", "_rm.json"));
					System.out.println("Done.");
					System.out.print("Creating the LocManager...");
					//LocManager lm = new LocManager(instLIRP);
					LocManager lm = null;
					System.out.println("Done.");
					System.out.print("Solving...");
					System.setOut(printStreamLog);
					System.setErr(printStreamLog);
					long startChrono = System.currentTimeMillis();
					Solution sol = Matheuristics.computeSolution(instLIRP, rm, withLoops, splitParam, lm);
					sol.computeObjValue();

					long stopChrono = System.currentTimeMillis();
					long duration = (stopChrono - startChrono);
					System.out.println("==================================================");
					System.out.println("Time to solve the instance: " + duration + " milliseconds");
					System.out.println("=================================================");
					System.out.println();
					
					System.setOut(original);
					System.out.println("Done.");


					if (sol != null){
						/* Stream for the solution */
						PrintStream printStreamSol = new PrintStream(fileSol);
						System.out.println("Printing the solution in " + fileNameSol);
						JSONParser.writeJSONToFile(sol.getJSONSol(), fileNameSol);
						printStreamSol.println("Resolution time : " + duration + " milliseconds");
						printStreamSol.println();
						printStreamSol.close();
					}
					else {
						System.out.println("Error on this instance");
					}

					System.out.println("Instance solved.");
				}
			}
			System.out.println("All Instances solved. FINISHED :-)");
		}

		catch (IOException ioe) {
			System.out.println("Error: " + ioe.getMessage());
		}
	}
}
