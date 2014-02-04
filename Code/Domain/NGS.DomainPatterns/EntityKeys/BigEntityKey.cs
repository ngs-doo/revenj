using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace NGS.DomainPatterns
{
    public static class BigEntityKey<TEntity>
    {
        private static long NextID = - 1;

        public static long GetNextID()
        {
            return --NextID;
        }
    }
}
