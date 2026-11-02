using System;

namespace Cs.Indexers
{
    public static class Program
    {
        private struct Trip : IComparable<Trip>
        {
            public int cost;
            public int days;

            public int CompareTo(Trip other)
            {
                return -this.cost.CompareTo(other.cost);
            }
        }

        public static void Main()
        {
            int n = int.Parse(Console.ReadLine());
            var trips = new Trip[n];
            for (int i = 0; i < n; i++)
            {
                var /*type:string[]*/input = Console.ReadLine().Split(' ');
                trips[i].cost = int.Parse(/*type:string*/input[1]);
                trips[i].days = int.Parse(input[0]);
            }
            Array.Sort(trips);
            int[,] totalProfit = new int[n + 1, n + 1];
            int maxProfit = 0;
            for (int i = 0; i < n; i++)
            {
                for (int j = 0; j < n; j++)
                {
                    totalProfit[i + 1, j + 1] = Math.Max(totalProfit[i, j] + trips[i].cost * (trips[i].days - j), totalProfit[i + 1, j + 1]);
                    maxProfit = Math.Max(maxProfit, totalProfit[i + 1, j + 1]);
                }
            }
            Console.WriteLine(maxProfit);
        }
    }
}
