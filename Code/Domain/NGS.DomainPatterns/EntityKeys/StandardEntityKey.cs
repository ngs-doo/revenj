using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace NGS.DomainPatterns
{
    public static class StandardEntityKey<TEntity>
    {
        private static int NextID = -1;

        public static int GetNextID()
        {
            return --NextID;
        }
    }
}
