package pet.skillbox.sitesearchengine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories
public class SiteSearchEngineApplication {

	public static void main(String[] args) {
		SpringApplication.run(SiteSearchEngineApplication.class, args);
//		Scanner scanner = new Scanner(System.in);
//		int N = scanner.nextInt();
//		double start = System.currentTimeMillis();
//		System.out.println(getNumber(N));
//		System.out.println((double)(System.currentTimeMillis() - start)/1000);
	}

	public static int getNumber(int N) {
		int count = 0;
		System.out.println("X max : " + (N / 50 + 100));
		System.out.println("Y max : " + (N / 100 + 100));
		System.out.println("Z max : " + (N / 200 + 100));
		if (N % 50 == 0) {
			for (int z = 0; z <= N / 200; ++z){
				for (int y = 0; y <= N / 100; ++y){
					for (int x = 0; x <= N / 50; ++x){
//						System.out.print("X : " + x);
//						System.out.print("   Y : " + y);
//						System.out.println("   Z : " + z);
						int tmp = 50 * x + 100 * y + 200 * z;

//						System.out.println(tmp);
						if (tmp == N) {
							++count;
						}
					}
				}
			}
		}
		return count;
	}
}
