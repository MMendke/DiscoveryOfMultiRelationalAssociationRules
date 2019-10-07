package net.sansa_stack.template.spark.rdf

import org.apache.spark.sql.{SQLContext,SparkSession}
import org.semanticweb.owlapi.model._
import net.sansa_stack.owl.spark.owl._
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkContext, SparkConf}

object TripleReader 
{
      val sparkContext = loadSparkContext()
      val sqlContext = loadSqlContext()
      val fileName = "transportAir"
      def loadSparkContext(): SparkContext = 
      {
            val conf = new SparkConf()
            .setAppName("Triple reader example") // replace with app name
            .setMaster("local")// we are running Spark on a local machine
            .set("spark.sql.crossJoin.enabled", "true")                              
            new SparkContext(conf)
      }
      def loadSqlContext(): SQLContext = 
      {
            val context = new org.apache.spark.sql.SQLContext(sparkContext)
            import context.implicits._
            context    
      }
      def main(args: Array[String]):Unit =
       {
            run(args(0))
       }
       def run(filePath: String)
       {
            type OWLAxiomsRDD = RDD[OWLAxiom]
            lazy val spark = SparkSession.builder().appName("Triple reader example  $filePath").master("local[*]")
            .config("spark.kryo.registrator","net.sansa_stack.owl.spark.dataset.UnmodifiableCollectionKryoRegistrator")
            .getOrCreate()
            
            var _rdd: OWLAxiomsRDD = null
            val syntax = Syntax.FUNCTIONAL
            //val filePath = this.getClass.getClassLoader.getResource(fileName+".owl").getPath
        
            def rdd: OWLAxiomsRDD = 
            {
                  if (_rdd == null)
                  {
                        _rdd = spark.owl(syntax)(filePath)
                        _rdd.cache()
                  }
                  _rdd
            }
            
            println("======================================================================================")
            println("|  Rules		| Confidence Value| Fitness Value|")
            println("======================================================================================")
           
            val filteredRDD= rdd.filter(axiom => axiom match {
            case a: OWLObjectPropertyAssertionAxiom => true
            case _ => false})
            
            //filteredRDD.foreach(println)
            val propDetailRDD = rdd.filter(f=>(f.isInstanceOf[OWLObjectPropertyDomainAxiom]||f.isInstanceOf[OWLObjectPropertyRangeAxiom]))
            
            Preprocessing.run(spark,filteredRDD,propDetailRDD)
            spark.stop()
             
       }
}