import React from 'react';

function SkeletonCard() {
  return (
    <div className="bg-slate-850/50 border border-slate-800 rounded-2xl p-4 flex flex-col justify-between animate-pulse h-full min-h-[260px]">
      <div>
        {/* Image Placeholder */}
        <div className="bg-slate-800 rounded-xl h-28 w-full mb-4"></div>
        
        {/* Name and Price Placeholder */}
        <div className="flex justify-between items-start gap-4">
          <div className="h-4 bg-slate-800 rounded w-2/3"></div>
          <div className="h-4 bg-slate-800 rounded w-1/4"></div>
        </div>
      </div>

      <div className="mt-4 flex items-center justify-between">
        {/* Stock Placeholder */}
        <div className="flex flex-col gap-1.5">
          <div className="h-2 bg-slate-800 rounded w-8"></div>
          <div className="h-3 bg-slate-800 rounded w-16"></div>
        </div>

        {/* Button Placeholder */}
        <div className="h-7 bg-slate-800 rounded-xl w-16"></div>
      </div>
    </div>
  );
}

export default SkeletonCard;
