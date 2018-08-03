import java.util.*;

public class lab0 {

	public static void main(String[] args) {
		int[] one = randArray();
		int[] two = one;
		int[] three = one;
		for(int x : one) {
			System.out.println(x);
		}
		System.out.println("Insertion");
		long startTimeOne = System.nanoTime();
		insertionSort(one);
		long endTimeOne = System.nanoTime();
		for(int x : one) {
			System.out.println(x);
		}
		System.out.println("Quick");
		long startTimeTwo = System.nanoTime();
		quickSort(one, 0, (one.length - 1));
		long endTimeTwo = System.nanoTime();
		for(int x : two) {
			System.out.println(x);
		}
		System.out.println("Radix");
		long startTimeThree = System.nanoTime();
		radixSort(three, three.length);
		long endTimeThree = System.nanoTime();
		for(int x : three) {
			System.out.println(x);
		}
		System.out.println("Time in nanoseconds for Insertion: " + (endTimeOne - startTimeOne));	
		System.out.println("Time in nanoseconds for Quick: " + (endTimeTwo - startTimeTwo));	
		System.out.println("Time in nanoseconds for Radix: " + (endTimeThree - startTimeThree));
	}

	static public void insertionSort(int[] a) {
		for(int j = 1 ; j < a.length ; j++) {
			int key = a[j];
			int i = j - 1;
			while(i >= 0 && a[i] > key) {
				a[i + 1] = a[i];
				i = i - 1;
			}
			a[i + 1] = key;
		}
	}
	
	static public void quickSort(int[] A, int p, int r) {
		if(p < r) {
			int q = partition(A, p, r);
			quickSort(A, p, q-1);
			quickSort(A, q+1, r);
		}
	}
	
	static public int partition(int[] A, int p, int r) {
		int x = A[r];
		int i = p-1;
		for (int j = p ; j < r ; j++) {
			if(A[j] <= x) {
				i = i + 1;
				int tem = A[i];
				A[i] = A[j];
				A[j] = tem;
			}
		}
		int temp = A[i + 1];
		A[i + 1] = A[r];
		A[r] = temp;
		return i + 1;
	}
	
	static public void radixSort(int[] arr, int n) {
		int m = getMax(arr, n);
		
		for (int exp = 1 ; m/exp > 0 ; exp *= 10) {
			countSort(arr, n, exp);
		}
	}
	
	static public void countSort(int[] arr, int n, int exp) {
		int[] output = new int[n];
		int i;
		int count[] = new int[10];
		Arrays.fill(count, 0);
		
		for( i = 0 ; i < n ; i++) {
			count[ (arr[i]/exp)%10 ]++;
		}
		
		for( i = 1 ; i < 10 ; i++) {
			count[i] += count[i-1];
		}
		
		for (i = n - 1 ; i >=0 ; i--) {
			output[count[ (arr[i]/exp)%10] - 1] = arr[i];
			count[ (arr[i]/exp)%10]--;
		}
		for (i = 0; i < n; i++) {
	        arr[i] = output[i];
		}
	}
	
	static public int getMax(int[] arr, int n) {
		int max = arr[0];
		for(int i = 1 ; i < n ; i++) {
			if(arr[i] > max) {
				max = arr[i];
			}
		}
		return max;
	}
	
	static public int[] randArray() {
		Random rand = new Random();
		int maxRandom = 100;
		int[] arr = new int[rand.nextInt(maxRandom)];
		for(int i = 0 ; i < arr.length ; i++) {
			arr[i] = rand.nextInt(maxRandom);
		}
		return arr;
	}
	
	
}
